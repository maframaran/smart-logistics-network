# ADR-002 — Apache Kafka for Async Integration

**Status:** Accepted

---

## Context

Six domain services (Shipment, Fleet, Driver, Routing, Warehouse, Billing) need to communicate when domain state changes. Options considered:

1. **Synchronous REST calls** between services
2. **Message broker (RabbitMQ)**
3. **Apache Kafka**

Synchronous REST creates tight coupling and cascading failures. RabbitMQ is a good fit for task queues but lacks the durable log replay and high-throughput partitioning needed for audit trails and future CQRS read models.

---

## Decision

Use **Apache Kafka** as the exclusive integration bus between services.

Reasons:
- **Durable log:** Events are retained (configurable per topic) enabling replay for new consumers and audit trails
- **High throughput:** Partitioned topics handle peak logistics volumes (shipment surges, fleet status storms)
- **Decoupling:** Services have zero compile-time or runtime dependency on each other — they only share event schemas (see [ADR-006](ADR-006-ddd.md))
- **Foundation for CQRS (Phase 3):** The event log can feed read-model projections without modifying producer services
- **Exactly-once semantics:** Kafka transactions prevent duplicate billing events

---

## Consequences

- Kafka runs in **KRaft mode** (no ZooKeeper) — simplifies Docker Compose setup (see [ADR-003](ADR-003-docker.md))
- All event payloads are JSON; Avro + Schema Registry is deferred to Phase 3
- Each service has its own consumer group ID; reprocessing is done by resetting offsets
- No service-to-service REST calls; all cross-domain data flows through topics defined in `architecture/integration.md`
- Dead-letter topics (`*.dlq`) are provisioned for each main topic to handle poison messages
