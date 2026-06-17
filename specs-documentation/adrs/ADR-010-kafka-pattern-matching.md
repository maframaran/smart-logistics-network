# ADR-010 — Java 21 Pattern-Matching Switch for Kafka Topic Routing

**Status:** Accepted

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
