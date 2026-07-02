# ADR-032 — Avro + Confluent Schema Registry

**Status:** Accepted

---

## Context

All Kafka producers were publishing domain events as JSON using Spring Kafka's `JsonSerializer`. While operationally fine for a single-team project, JSON provides no schema enforcement: a producer can add, rename, or remove a field and consumers break silently with no compile-time or deploy-time signal. As the number of topics grows (currently 13 event types across 6 producers) and more consumers are added in later stages (CQRS projectors in Stage 6, marketplace consumers in Stage 8), schema drift becomes a real operational risk.

The Transactional Outbox (ADR-030) from Stage 2 made the `XxxKafkaPublisher.publish()` method the single serialization chokepoint per service — the outbox relay calls it once per event, not every use case. This meant the Avro migration could be done in one place per service rather than scattered across every use case call site.

## Decision

**Wire format: Apache Avro** with one `.avsc` schema file per domain event, located in `<service>/src/main/avro/`. Code generation via `avro-maven-plugin` (version 1.11.3, `stringType=String`) produces `SpecificRecord` subclasses in the `com.logistics.<service>.avro` package inside each producing service. These generated classes are internal infrastructure; they are not exported by the service's JPMS `module-info.java`.

**Schema Registry: Confluent Schema Registry 7.6.0**, running as a Docker Compose service (`schema-registry`, host port 8090, internal port 8081). Added as a `healthcheck`-gated dependency for all 8 services. Schema Registry URL injected via `SCHEMA_REGISTRY_URL` env var (defaults to `http://localhost:8090` for local dev outside Docker).

**Compatibility mode:** default (`BACKWARD`) — new schema versions may only add optional fields (not remove or rename required ones). Not explicitly configured in this stage; Confluent's default applies.

**Subject naming:** `TopicNameStrategy` (Confluent default) — subjects are `<topic>-value` (e.g. `shipment.created-value`).

**Producer side (6 services):** Each `XxxKafkaPublisher` is injected with a new package-private `XxxEventAvroMapper` bean. Before calling `kafkaTemplate.send()`, the mapper converts the domain event to its corresponding `SpecificRecord`. Spring's auto-configured `KafkaTemplate` uses `KafkaAvroSerializer` (configured via `spring.kafka.producer.value-serializer`), which registers or fetches the schema from Schema Registry and serializes using Avro binary format. The `KafkaTemplate<String, Object>` type is unchanged; the SpecificRecord is passed as the value.

**Consumer side (notification-service, rag-service):** `value-deserializer` changed to `KafkaAvroDeserializer` (Confluent). Default behavior (`specific.avro.reader=false`) returns `org.apache.avro.generic.GenericRecord` — no generated classes needed on the consumer side, preserving module independence (ADR-004) and the typed-record-at-the-boundary convention (ADR-026). Each consumer service has a new package-private `XxxEventAvroMapper` that maps `GenericRecord` → existing typed DTO records. The listener method parameter type changes from the typed DTO to `GenericRecord`; the mapper call is the first line in each listener body. The typed DTO records (ADR-026) and the application/domain code are unchanged.

**JPMS:** `requires org.apache.avro;` added to all 7 module-info.java files (6 producers + notification-service). Apache Avro 1.11.x ships with a proper `module-info.class` (module name `org.apache.avro`). `kafka-avro-serializer` is referenced only via class-name string in `application.yml` — Spring Boot reflectively loads the serializer/deserializer class — so no `requires` directive for Confluent's jar is needed.

**Avro schema design choices:**
- UUID → `"string"` (toString on produce; parsed if needed on consume)
- `Instant` → `"long"` (epoch milliseconds; not using `timestamp-millis` logical type to avoid avro-maven-plugin logical type conversion config)
- `LocalDate` → `"int"` (epoch days; same reason)
- `BigDecimal` (Money.amount) → `"string"` (toPlainString on produce; `new BigDecimal(str)` on consume)
- Enums (SlaType, VehicleType, etc.) → `"string"` (`.name()` on produce; `.toString()` on GenericRecord)
- Nested value objects (Address, CargoSpec) → inline Avro record definitions within the event schema
- Capacity (fleet) → flattened into `maxWeightKg` / `maxVolumeM3` fields directly on the event schema
- Money (billing) → flattened into `<field>Brl` + `<field>Currency` pairs

## Alternatives Considered

- **JSON Schema + Schema Registry**: Would keep JSON on the wire, adding only schema enforcement via Confluent's JSON Schema serializer. Rejected — Avro's binary format is more compact and is the industry standard; the migration cost is similar either way since both require schema files.
- **SpecificRecord on consumer side (shared .avsc)**: Would give type-safe consumer code but requires the consumer to have the producer's .avsc file available (breaking module independence, ADR-004). GenericRecord + mapper achieves the same result without coupling.
- **Protobuf**: Feature-equivalent to Avro, also supported by Confluent Schema Registry. Rejected — Avro is already used in the Confluent ecosystem we're running on, and the avro-maven-plugin generates idiomatic Java with Builder APIs. No compelling reason to switch.

## Consequences

- Schema Registry becomes a required infrastructure dependency for all 8 services — they will fail to start if Schema Registry is unreachable. Schema Registry itself requires Kafka to be healthy first.
- Schema incompatible changes (renaming a required field, removing a field, changing a field type) are rejected at publish time by Schema Registry. This is the intended correctness guarantee.
- Adding a new event field requires: updating the `.avsc` file (adding with a default so BACKWARD compat is preserved), updating the producer mapper, updating the consumer mapper if the consumer needs the field.
- The `shipment.delivered` topic has no Avro schema yet (shipment-service has no `ShipmentDelivered` domain event). The notification-service listener for this topic is a no-op at runtime (no messages published), but would receive `GenericRecord` if a producer were added later without a registered schema — which would fail at deserialization. The schema must be registered before adding a producer.
- Pre-existing "null field" gaps in consumer DTOs (e.g. `originCity`, `warehouseName`, `slaType` in rag-service DTOs were already null in the JSON era) remain null — these fields are not present in the producer's Avro schema, so no field name was available to map them. These gaps are explicitly documented in the mapper with comments.

## Related

- [ADR-004 — JPMS](ADR-004-jpms.md)
- [ADR-026 — Typed Records Over Maps](ADR-026-typed-records-over-maps.md)
- [ADR-027 — Kafka Topic Config Dispatch](ADR-027-kafka-topic-config-dispatch.md)
- [ADR-030 — Transactional Outbox](ADR-030-transactional-outbox.md) (Stage 2, makes relay the single serialization chokepoint)
- Phase 3+4+5 implementation plan — this is Stage 3
