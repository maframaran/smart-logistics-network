# ADR-027 — Map-Based Kafka Topic Dispatch with Externalized Topic Names

**Status:** Accepted

---

## Context

A SonarQube static-analysis pass flagged 12 `S1481` ("unused variable") findings across all 6 `XxxKafkaPublisher` classes (`ShipmentKafkaPublisher`, `VehicleKafkaPublisher`, `DriverKafkaPublisher`, `WarehouseKafkaPublisher`, `BillingKafkaPublisher` — `RouteKafkaPublisher` used a single-type `instanceof` check, not the pattern below), all on the `ignored` binding in the pattern-matching switch dispatch from [ADR-010](ADR-010-kafka-pattern-matching.md):

```java
private String topicFor(DomainEvent event) {
    return switch (event) {
        case ShipmentCreated   ignored -> TOPIC_CREATED;
        case ShipmentAssigned  ignored -> TOPIC_ASSIGNED;
        case ShipmentCancelled ignored -> TOPIC_CANCELLED;
        default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
    };
}
```

This is a structural false positive: Java's type-pattern `case` syntax requires a binding name even when unused, and there is no way to omit it in stable Java 21 (Java 22 added `case ShipmentCreated _ ->`, confirmed by test-compiling both forms against this project's pinned JDK — see ADR-001 for why Java 21 LTS is pinned). Suppressing the finding (`// NOSONAR`) was considered but rejected in favor of removing the pattern at its root, which also happened to resolve it.

Two designs were evaluated for the replacement (see Alternatives), both prompted by wanting topic names to live in configuration rather than as `private static final String` constants scattered across publisher classes.

## Decision

Each Kafka publisher builds a `Map<Class<? extends DomainEvent>, String>` in its constructor from `@Value`-injected `application.yml` properties, and `publish()` becomes a single map lookup:

```java
@Component
public class ShipmentKafkaPublisher implements ShipmentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<Class<? extends DomainEvent>, String> topics;

    public ShipmentKafkaPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topics.shipment-created}") String topicCreated,
            @Value("${kafka.topics.shipment-assigned}") String topicAssigned,
            @Value("${kafka.topics.shipment-cancelled}") String topicCancelled) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = Map.of(
                ShipmentCreated.class, topicCreated,
                ShipmentAssigned.class, topicAssigned,
                ShipmentCancelled.class, topicCancelled
        );
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = topics.get(event.getClass());
        if (topic == null) throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        kafkaTemplate.send(topic, event.aggregateId(), event);
    }
}
```

```yaml
# application.yml
kafka:
  topics:
    shipment-created: shipment.created
    shipment-assigned: shipment.assigned
    shipment-cancelled: shipment.cancelled
```

The same `kafka.topics.<event-kebab-case>` key convention applies across all 6 services. The `IllegalArgumentException` safety net from ADR-010 is preserved — a `null` map lookup still fails loudly rather than silently dropping the event.

## Alternatives Considered

- **`DomainEvent.topic()` abstract method + a shared `KafkaTopic` enum in `common`.** Each domain event record would override `topic()` to return its own enum constant, giving compile-time exhaustiveness (forgetting a topic is a compile error, not a runtime one). Rejected for two reasons: (1) it requires every domain event across every service to reference a Kafka-routing concept, which is an infrastructure concern leaking into the domain layer — a violation of the project's hexagonal rule that nothing in `domain/` may know about messaging/infrastructure (see [ADR-005](ADR-005-hexagonal.md)); (2) combined with the externalized-config requirement below, it's not just undesirable but technically unworkable — domain event records are plain value objects constructed via static factories, never Spring-managed beans, so `application.yml` values cannot be injected into them at all without a static service-locator anti-pattern.
- **Keep topic names as `private static final String` constants, just replace the `switch` with a `Map.of(...)` of hardcoded strings.** Solves the `S1481` finding but not the actual goal of externalizing topic names to configuration. Rejected as half-measure.

## Consequences

- New event types require adding a `@Value`-injected constructor parameter and a `Map.of(...)` entry in the owning publisher, plus a `kafka.topics.<key>` entry in that service's `application.yml`. Missing either is still caught — a missing `application.yml` property fails Spring context startup; a missing map entry still throws `IllegalArgumentException` at publish time, same as before.
- Topic names are now environment-configurable (useful for topic-name prefixing per environment, blue/green naming, etc.) without code changes — previously this required editing the `private static final String` constants.
- Kafka **consumers** (`@KafkaListener(topics = "shipment.created", ...)`) still hardcode topic strings as annotation literals; they were not in scope for this change (no `S1481` finding there — listener methods don't dispatch). Spring supports `@KafkaListener(topics = "${kafka.topics.shipment-created}")` for the same externalization on the consumer side, which would give each service a true single source of truth for its topic names; this is a natural follow-up, not yet done.

## Related

- [ADR-010 — Java 21 Pattern-Matching Switch for Kafka Topic Routing](ADR-010-kafka-pattern-matching.md) (superseded by this ADR)
- [ADR-002 — Kafka](ADR-002-kafka.md)
- [ADR-005 — Hexagonal Architecture](ADR-005-hexagonal.md)
- [ADR-026 — Typed Records Over Maps](ADR-026-typed-records-over-maps.md) (same audit-driven origin — SonarQube findings prompting a structural fix rather than a suppression)
