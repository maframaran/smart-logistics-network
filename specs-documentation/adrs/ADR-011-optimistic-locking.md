# ADR-011 — Optimistic Locking via `@Version` on JPA Entities

**Status:** Accepted

---

## Context

Multiple service instances may process concurrent requests affecting the same aggregate (e.g., two requests both attempt to assign the same shipment). Without concurrency control, the last writer silently wins and can corrupt state.

---

## Decision

Every JPA entity in every service carries a `@Version Long version` field managed by JPA/Hibernate:

```java
@Version
private Long version;
```

This enables **optimistic locking**: when an entity is updated, Hibernate includes `WHERE version = <expected>` in the SQL. If another transaction already incremented the version, a `OptimisticLockException` is thrown and propagated as a 409 Conflict by the `GlobalExceptionHandler`.

The `version` column is included in every Flyway migration's `CREATE TABLE` statement with `DEFAULT 0`.

---

## Consequences

- No explicit database-level locks needed for normal request handling
- Throughput is higher than pessimistic locking under low contention (the common case)
- Under high contention, callers receive a 409 and must retry — acceptable for the logistics domain where true simultaneous conflicts on one aggregate are rare
- Every new JPA entity must include the `@Version` field; enforced by code review
