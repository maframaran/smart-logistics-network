# ADR-012 — Per-Service Flyway Schemas in Shared PostgreSQL

**Status:** Accepted

---

## Context

All services share one PostgreSQL instance in the local Docker Compose environment. Tables from different services must not collide, and each service must own its schema migrations independently.

---

## Decision

Each service places its tables in a dedicated **PostgreSQL schema** (not the default `public` schema). Flyway is configured per service to target that schema:

| Service | Schema |
|---------|--------|
| shipment-service | `shipment` |
| fleet-service | `fleet` |
| driver-service | `driver` |
| routing-service | `routing` |
| warehouse-service | `warehouse` |
| billing-service | `billing` |
| notification-service | `notification` |

Each service's `application.yml` sets:

```yaml
spring:
  flyway:
    schemas: <schema-name>
    default-schema: <schema-name>
  jpa:
    properties:
      hibernate:
        default_schema: <schema-name>
```

Migration files live in `src/main/resources/db/migration/` of each service module and are prefixed `V1__`, `V2__`, etc. independently.

---

## Consequences

- Schema isolation in a single DB — no table naming collisions between services
- Each service owns its migration history (`flyway_schema_history` is per schema)
- Moving to dedicated databases per service (Phase 3) requires only connection-string changes, not schema restructuring
- Database credentials are shared across services in the local environment; in production each service should have its own DB user scoped to its schema
