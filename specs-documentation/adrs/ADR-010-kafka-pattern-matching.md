# ADR-010 — Java 21 Pattern-Matching Switch for Kafka Topic Routing

**Status:** Superseded by [ADR-027](ADR-027-kafka-topic-config-dispatch.md)

> A SonarQube static-analysis pass (`S1481`) flagged the `case EventType ignored ->` bindings below as unused variables across all 6 publishers — a structural false positive in stable Java 21 (no syntax exists to omit the binding without bumping to Java 22's unnamed-pattern `_` syntax). Rather than suppress the warning, the dispatch mechanism itself was replaced with a `Map<Class<? extends DomainEvent>, String>` built from externally configured topic names (see ADR-027), which also resolves this ADR's own noted downside ("not compatible with older LTS versions" / requires Java 21 pattern matching) and externalizes topic names to `application.yml` as a side benefit. Kept here for history.

---

## Context

Each service's Kafka publisher must map a `DomainEvent` instance to the correct Kafka topic string. A chain of `instanceof` checks or a parallel dispatch table both require manual maintenance when new event types are added.

---

## Decision

Kafka publishers use **Java 21 pattern-matching switch expressions** to route events to topics:

```java
private String topicFor(DomainEvent event) {
    return switch (event) {
        case ShipmentCreated   ignored -> TOPIC_CREATED;
        case ShipmentAssigned  ignored -> TOPIC_ASSIGNED;
        case ShipmentCancelled ignored -> TOPIC_CANCELLED;
        default -> throw new IllegalArgumentException(
            "Unknown event type: " + event.getClass().getSimpleName());
    };
}
```

The `default` branch throws immediately, making missing event-type mappings a hard runtime failure rather than a silent no-op.

---

## Consequences

- The compiler enforces exhaustive handling when sealed types are used (future improvement: make `DomainEvent` sealed)
- `default` guard ensures new event types are caught at test time if not mapped
- The pattern is uniform across all publisher classes (`ShipmentKafkaPublisher`, `FleetKafkaPublisher`, etc.)
- Java 21 is required; not compatible with older LTS versions
