# ADR-025 — Spring Data JPA as the Standard Persistence Mechanism

**Status:** Accepted

---

## Context

Hexagonal architecture ([ADR-005](ADR-005-hexagonal.md)) defines `infrastructure/persistence/` as the outbound adapter layer behind each service's domain repository port, but does not itself mandate a specific persistence technology — a service could in principle use JPA, plain JDBC, or another mapper.

An audit of all 8 backend service modules was performed to confirm what persistence technology is actually in use, ahead of a request to standardize the codebase on Spring Data JPA. The audit found the codebase is already consistent: every service follows the same three-layer adapter pattern (domain port → `*JpaRepository` adapter → `*JpaRepositoryPort extends JpaRepository<Entity, ID>` → `@Entity`), using `@Version` for optimistic locking ([ADR-011](ADR-011-optimistic-locking.md)) and a dedicated Flyway-managed schema per service ([ADR-012](ADR-012-per-service-flyway-schemas.md)).

The single exception is `rag-service`, whose `PgVectorStoreAdapter` (`rag-service/src/main/java/com/logistics/rag/infrastructure/persistence/PgVectorStoreAdapter.java`) uses `JdbcTemplate` with raw SQL for pgvector ANN similarity search. This was a deliberate choice already recorded in [ADR-024](ADR-024-rag-pgvector.md): the `vector(1536)` column type and cosine-distance operator (`embedding <=> ?`, bound via the `com.pgvector:pgvector` JDBC type) have no representation in JPQL or Spring Data query derivation.

This ADR formalizes Spring Data JPA as the codebase-wide standard and records the rag-service exception explicitly, so the existing (already-correct) pattern doesn't erode as new services or repositories are added.

---

## Decision

**All services use Spring Data JPA (`JpaRepository`) as the standard persistence mechanism for their repository adapters.** The single sanctioned exception is `rag-service`'s `PgVectorStoreAdapter`, which uses `JdbcTemplate` for pgvector ANN similarity queries, because Spring Data JPA cannot express the `vector` column type or the `<=>` cosine-distance operator.

Audit evidence — all 7 non-RAG services confirmed compliant:

| Service | Repository Adapter | Spring Data Proxy | Entity |
|---|---|---|---|
| shipment-service | [`ShipmentJpaRepository`](../../shipment-service/src/main/java/com/logistics/shipment/infrastructure/persistence/ShipmentJpaRepository.java) | `ShipmentJpaRepositoryPort extends JpaRepository<ShipmentJpaEntity, UUID>` | `ShipmentJpaEntity` |
| fleet-service | [`VehicleJpaRepository`](../../fleet-service/src/main/java/com/logistics/fleet/infrastructure/persistence/VehicleJpaRepository.java) | `VehicleJpaRepositoryPort extends JpaRepository<VehicleJpaEntity, UUID>` | `VehicleJpaEntity` |
| driver-service | [`DriverJpaRepository`](../../driver-service/src/main/java/com/logistics/driver/infrastructure/persistence/DriverJpaRepository.java) | `DriverJpaRepositoryPort`, `DrivingSessionJpaRepositoryPort` (both `extends JpaRepository`) | `DriverJpaEntity`, `DrivingSessionJpaEntity` |
| routing-service | [`RouteJpaRepository`](../../routing-service/src/main/java/com/logistics/routing/infrastructure/persistence/RouteJpaRepository.java) | `RouteJpaRepositoryPort extends JpaRepository<RouteJpaEntity, UUID>` | `RouteJpaEntity`, `RouteSegmentJpaEntity` (`@OneToMany`) |
| warehouse-service | [`WarehouseJpaRepository`](../../warehouse-service/src/main/java/com/logistics/warehouse/infrastructure/persistence/WarehouseJpaRepository.java) | `WarehouseJpaRepositoryPort extends JpaRepository<WarehouseJpaEntity, UUID>` | `WarehouseJpaEntity`, `InventoryItemJpaEntity` (`@OneToMany`) |
| billing-service | [`InvoiceJpaRepository`](../../billing-service/src/main/java/com/logistics/billing/infrastructure/persistence/InvoiceJpaRepository.java) | `InvoiceJpaRepositoryPort extends JpaRepository<InvoiceJpaEntity, UUID>` | `InvoiceJpaEntity` |
| notification-service | [`NotificationJpaRepository`](../../notification-service/src/main/java/com/logistics/notification/infrastructure/persistence/NotificationJpaRepository.java) | `NotificationJpaRepositoryPort extends JpaRepository<NotificationJpaEntity, UUID>` | `NotificationJpaEntity` |
| rag-service (exception) | [`PgVectorStoreAdapter`](../../rag-service/src/main/java/com/logistics/rag/infrastructure/persistence/PgVectorStoreAdapter.java) — `JdbcTemplate`, raw SQL | n/a | n/a — pgvector columns, no relational entity |

All 7 compliant services use only derived-finder query methods (e.g. `findByStatus`); none have `@Query` or native SQL, and none use `JdbcTemplate` or `EntityManager` directly.

---

## Alternatives Considered

- **Force rag-service onto Spring Data JPA via `@Query(nativeQuery = true)`.** Technically possible to route the pgvector SQL through a `JpaRepository` interface method, but it would still be raw SQL underneath, while adding JPA entity-mapping overhead for `embedding`/vector columns that carry no relational semantics and are never updated through entity lifecycle events. Rejected — no benefit over the current `JdbcTemplate` adapter.
- **Move all services to `JdbcTemplate` for uniformity with rag-service.** Rejected — would discard Spring Data JPA's derived-query methods and `@Version`-based optimistic locking for the 7 services that are simple CRUD-shaped aggregates, in favor of uniformity with the one service that has a genuine technical reason to deviate.

---

## Consequences

- New services and new repository adapters must default to Spring Data JPA (`JpaRepository`) unless there is a non-relational query need equivalent to rag-service's vector search.
- Any future `JdbcTemplate`, `EntityManager`, or native-SQL usage outside `rag-service` requires its own ADR following the precedent set by [ADR-024](ADR-024-rag-pgvector.md), justifying why Spring Data JPA query derivation/`@Query` JPQL is insufficient.
- `specs-documentation/docs/code-creation-guide.md` is updated to cross-reference this ADR as the binding rule for the JPA entity + adapter step.
- `specs-documentation/services/rag-service/service.md` is updated to note it is the sanctioned exception to this standard.

---

## Related

- [ADR-005 — Hexagonal Architecture](ADR-005-hexagonal.md)
- [ADR-011 — Optimistic Locking](ADR-011-optimistic-locking.md)
- [ADR-012 — Per-Service Flyway Schemas](ADR-012-per-service-flyway-schemas.md)
- [ADR-024 — RAG with pgvector and Claude API](ADR-024-rag-pgvector.md)
