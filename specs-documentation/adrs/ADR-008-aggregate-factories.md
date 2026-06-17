# ADR-008 — Aggregate Factory Pattern: `create()` vs `reconstitute()`

**Status:** Accepted

---

## Context

Aggregates have two distinct construction paths:

1. **New aggregate** — created in response to a command; must validate invariants, assign a generated ID, and raise a domain event.
2. **Reconstructed aggregate** — rehydrated from the database; invariants were already validated when the aggregate was first persisted; no event should be raised again.

Using a single constructor for both cases leads to either skipping validation on new aggregates, or raising spurious domain events on every database read.

---

## Decision

Every aggregate exposes two static factory methods and a private constructor:

```java
// New aggregate: validates, generates ID, raises domain event
public static Shipment create(String shipperId, Address origin, ...) { ... }

// Rehydration from persistence: no validation, no event
public static Shipment reconstitute(ShipmentId id, String shipperId, ...) { ... }
```

The constructor is private. All callers go through one of the two factory methods.

### Rules

- `create()` — validates all invariants, generates `ShipmentId.generate()` (random UUID), calls `registerEvent()`
- `reconstitute()` — sets fields directly, skips validation, does **not** call `registerEvent()`
- JPA repository adapters call `reconstitute()` when mapping `XxxJpaEntity → Aggregate`
- Application use cases call `create()` when handling a command

---

## Consequences

- Domain events are raised exactly once per state transition — never duplicated on reads
- Validation logic is collocated with `create()` and easy to test in isolation
- `reconstitute()` acts as a trusted internal path; misuse (bypassing validation during command handling) is a code-review concern, not a runtime concern
