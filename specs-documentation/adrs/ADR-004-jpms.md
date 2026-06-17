# ADR-004 — Java Platform Module System (JPMS)

**Status:** Accepted

---

## Context

With multiple domain services in a single Maven multi-module build, there is a risk of accidental compile-time coupling between domains. A developer could `import com.logistics.fleet.Vehicle` directly inside the shipment domain, bypassing the intended decoupling enforced at runtime by Kafka. Past experience shows that without hard boundaries, cross-domain dependencies accumulate silently.

---

## Decision

Use the **Java Platform Module System (JPMS)** with a `module-info.java` in each domain module to enforce boundaries at compile time.

Module naming convention: `com.logistics.<domain>` (e.g. `com.logistics.shipment`)

Rules encoded in `module-info.java`:
- Domain modules `exports` only their public API (ports, domain events, value objects)
- Domain modules do NOT `requires` infrastructure modules (Spring, JPA, Kafka)
- Infrastructure adapter modules `requires` the domain module and the framework

Example:
```java
// com.logistics.shipment (domain module)
module com.logistics.shipment {
    exports com.logistics.shipment.domain;
    exports com.logistics.shipment.domain.events;
    exports com.logistics.shipment.domain.ports;
    requires com.logistics.common;
}

// com.logistics.shipment.infrastructure (adapter module)
module com.logistics.shipment.infrastructure {
    requires com.logistics.shipment;
    requires spring.boot;
    requires spring.kafka;
    requires jakarta.persistence;
}
```

The full module dependency graph is in `architecture/integration.md`.

---

## Consequences

- Illegal cross-domain access fails at compile time, not at runtime
- Each domain can evolve its internal packages freely; only exported packages are contractual
- JPMS requires `opens` declarations for reflection-heavy frameworks (Spring, Hibernate) — these go in the infrastructure module, keeping the domain module clean
- Increases initial setup complexity but pays off as the number of services grows
- See [ADR-005](ADR-005-hexagonal.md) for how JPMS module boundaries reinforce the hexagonal architecture layer rule
