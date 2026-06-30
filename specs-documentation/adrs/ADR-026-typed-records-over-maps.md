# ADR-026 — Typed Records Instead of Map&lt;String, Object&gt; for Structured Data

**Status:** Accepted

---

## Context

A first audit of `@KafkaListener` methods found 8 listeners — all 4 in `rag-service`'s `RagKafkaConsumer` and all 4 in `notification-service`'s `ShipmentEventConsumer` — deserializing Kafka message payloads into `Map<String, Object>` and reading fields with `payload.get("someKey")` or `(String) payload.getOrDefault("key", "default")`. This loses compile-time type safety: typos in map keys, wrong cast types, and missing-field bugs only surface at runtime, and the shape of the expected message isn't visible from the method signature.

A follow-up audit, prompted by a user spotting that `rag-service`'s `PgVectorStoreAdapter` had the exact same problem, found the anti-pattern in two more places along the same boundary:

1. **`VectorStorePort`** (`rag-service/domain/ports/out/VectorStorePort.java`) — the hexagonal outbound port to pgvector used `Map<String, Object>` both as the upsert metadata parameter and as the return type of every ANN similarity query (`findSimilarRoutes`, `findSimilarInvoices`, `findSimilarShipments`, `findAllInventory`). Every caller in `rag-service`'s `application/usecases` package built these maps by hand and read query results back out with `row.get("total_cost_brl")`-style lookups, with separate `toDouble`/`toLong`/`str`/`orEmpty` casting helpers duplicated in five classes.
2. **`RagController`** (`rag-service/infrastructure/rest/RagController.java`) — two endpoints (`/invoices/{id}/waiver`, `/pricing/recommend`) took `@RequestBody Map<String, String>` / `@RequestBody Map<String, Object>` instead of typed request DTOs, despite the project's own naming convention (`code-creation-guide.md` Section D) already specifying `<Action><Entity>Request` records for REST request DTOs.

A full repo-wide sweep (all 9 service modules) for any other `Map<String, *>` used as a parameter or return type for structured/domain data found no further instances — the remaining `Map` usages in the codebase are legitimate: dynamic API request bodies sent to the Anthropic API (`ClaudeLlmAdapter`, `ClaudeEmbeddingAdapter`), aggregates keyed by a natural id with an already-typed value (`Warehouse.inventory: Map<String, InventoryItem>`, `Driver.drivingSessions: Map<LocalDate, DrivingSession>`), a genuinely dynamic group-by-month accumulator (`DemandForecastService.byMonth`), and a static config table (`PricingAdvisorService.STATIC_RATES`).

By contrast, every event **producer** (`ShipmentKafkaPublisher`, `RouteKafkaPublisher`, `InvoiceKafkaPublisher`, `WarehouseKafkaPublisher`) already publishes strongly-typed Java `record`s implementing `common`'s `DomainEvent` interface. The inconsistency was entirely on the *receiving* side of every boundary in `rag-service`/`notification-service`: Kafka consumers, the vector-store port, and REST request bodies.

`rag-service` and `notification-service` only depend on the `common` Maven module — neither lists `shipment-service`, `routing-service`, `billing-service`, or `warehouse-service` as a dependency. This is intentional per [ADR-004](ADR-004-jpms.md) (JPMS module boundaries): a consumer should not compile against another service's internal `domain/events` classes, since that would create a hard compile-time coupling between independently-deployable services.

## Decision

**Any method signature that carries structured, fixed-shape data across a boundary — a Kafka `@Payload`, a hexagonal port parameter/return type, or a REST `@RequestBody`/response — must use a typed Java `record`, never `Map<String, Object>` or `Map<String, String>`.** `Map` remains acceptable only for genuinely dynamic/unbounded keyed data (see the legitimate-usage list above).

For **Kafka consumers**: the record is locally owned in the consuming service (not imported from the producer's `domain/events` package), mirroring the JSON wire shape, scoped to the fields actually used. No extra Spring Kafka configuration is needed — with no `spring.json.value.default.type` set and no type-id headers on the wire (the default for Spring's `JsonSerializer`), the default `JsonDeserializer` infers its target type from the listener method's declared parameter type.

For **hexagonal ports backed by raw SQL** (like `VectorStorePort`, which can't use Spring Data JPA per [ADR-025](ADR-025-spring-data-jpa-standard.md)'s pgvector exception): define paired `*Metadata` (write-side) and `*Row`/`*SearchRow` (read-side) records in `domain/model/`, and build them in the adapter via an explicit `RowMapper` lambda (`jdbc.query(sql, (rs, rowNum) -> new XRow(rs.getString("..."), ...), params)`) instead of `jdbc.queryForList(...)`.

For **REST request bodies**: define a `<Action><Entity>Request` record in `infrastructure/rest/`, per the existing naming convention in `code-creation-guide.md` Section D — this was already the documented standard, just not followed in `RagController`.

Applied in this change:
- `rag-service/infrastructure/messaging/dto/`: `RouteCalculatedPayload`, `InvoiceGeneratedPayload` (+ `MoneyPayload`), `WarehouseCapacityUpdatedPayload`, `ShipmentCreatedPayload` (+ `AddressPayload`, `CargoSpecPayload`)
- `notification-service/infrastructure/messaging/dto/`: `ShipmentCreatedPayload`, `ShipmentAssignedPayload`, `ShipmentDeliveredPayload`, `ShipmentCancelledPayload`
- `rag-service/domain/model/`: `RouteMetadata`/`RouteSearchRow`, `InvoiceMetadata`/`InvoiceSearchRow`, `ShipmentMetadata`/`ShipmentSearchRow`, `InventoryMetadata`/`InventoryRow` — used by `VectorStorePort` and implemented via `RowMapper` lambdas in `PgVectorStoreAdapter`
- `rag-service/infrastructure/rest/`: `WaiverRecommendationRequest`, `PricingRecommendationRequest`

**Known gap, not fixed by this ADR:** `notification-service.onShipmentDelivered` listens to `shipment.delivered`, but `shipment-service` has no `ShipmentDelivered` domain event and never publishes to that topic — there's no `deliver()` lifecycle method on the `Shipment` aggregate. This listener has been dead code in production since it was written. `ShipmentDeliveredPayload` was typed against the documented schema in `specs-documentation/messaging/topics/shipment.delivered.md` for consistency, but implementing the actual producer is a separate, unscoped piece of work.

## Alternatives Considered

- **Import the producer's domain event record directly** (e.g. `rag-service` depending on `routing-service` for `RouteCalculated`). Rejected — breaks the module independence established by ADR-004; a consumer would fail to compile if the producer's internal domain model changed in a way unrelated to the wire contract.
- **Configure `spring.json.type.mapping` with the producer's fully-qualified class names.** Rejected — same coupling problem (the consumer's classpath/config would still need to reference the producer's class names), plus added per-topic configuration burden with no readability benefit over a locally declared record.
- **Use Spring's `BeanPropertyRowMapper` for the `VectorStorePort` query rows.** Rejected — it relies on setters/no-arg constructors and doesn't work cleanly against immutable records; an explicit `RowMapper` lambda is one line per column and keeps the SQL column → record field mapping visible at the call site.

## Consequences

- Any new Kafka consumer, JDBC-backed port, or REST endpoint must declare a typed record for its structured payload, rather than `Map<String, Object>`.
- Locally-owned DTOs (Kafka payloads, `VectorStorePort` metadata/rows) are a **consumer-owned contract**: they only need to include fields actually used, and must be kept in sync manually if the upstream JSON/SQL shape changes (no compiler-enforced link — the standard trade-off of consumer-driven contracts over shared classes).
- The five duplicated `toDouble`/`toLong`/`str`/`orEmpty` Map-casting helper methods across `rag-service`'s use-case classes are reduced to just `orEmpty` (still needed for null-safe Kafka payload string fields); the row-casting helpers are gone entirely now that `RowMapper` does typed extraction at the JDBC boundary.
- The pre-existing `shipment.delivered` gap is now explicitly documented rather than silently hidden inside ad hoc `Map` lookups.

## Related

- [ADR-004 — JPMS](ADR-004-jpms.md)
- [ADR-025 — Spring Data JPA Standard](ADR-025-spring-data-jpa-standard.md) (same audit-then-formalize shape; documents why `VectorStorePort` can't be plain Spring Data JPA)
