# ADR-006 — Domain-Driven Design (DDD)

**Status:** Accepted

---

## Context

The logistics domain is complex: multiple bounded contexts (Shipment, Fleet, Driver, Routing, Warehouse, Billing), rich business rules (BR-001 through BR-008), and intricate lifecycle state machines. Without explicit domain modeling, business rules scatter across service classes and become hard to locate, test, or change.

---

## Decision

Apply **Domain-Driven Design** tactical patterns throughout all services.

### Patterns in Use

| Pattern | Usage |
|---------|-------|
| **Aggregate** | `Shipment`, `Vehicle`, `Driver`, `Warehouse`, `Route`, `Invoice` — each enforces its own invariants |
| **Value Object** | `ShipmentId`, `CargoSpec`, `Address`, `Capacity`, `SKU`, `SlaPenalty` — immutable, equality by value |
| **Domain Event** | `ShipmentCreated`, `ShipmentAssigned`, etc. — raised inside aggregates, published by infrastructure adapters |
| **Repository (port)** | Defined as interfaces in the domain, implemented in the infrastructure layer |
| **Bounded Context** | One service per domain; inter-context communication only via Kafka events (see [ADR-002](ADR-002-kafka.md)) |
| **Ubiquitous Language** | Terms from `docs/overview.md` are used as-is in code: `Shipment`, `Carrier`, `DeliveryWindow`, `SlaType` |

### Business Rule Placement

Business rules live inside aggregates or domain services — never in REST controllers or Kafka consumers:

- **BR-001/002** (capacity): enforced inside `Vehicle.canAccommodate(CargoSpec)` or the assignment use case
- **BR-003** (hazmat certification): enforced inside `Driver.isEligibleFor(CargoSpec)`
- **BR-004** (SLA ETA): enforced inside `Shipment.validateEta(Route, DeliveryWindow)`
- **BR-005** (working hours): enforced inside `Driver.logDrivingHours(duration)`
- **BR-006** (warehouse capacity): enforced inside `Warehouse.receive(InventoryItem)`
- **BR-007** (cancellation): enforced inside `Shipment.cancel(CancellationRequest)` via state machine
- **BR-008** (cold chain): enforced alongside BR-003 in assignment validation

### Aggregate Invariant Rule

An aggregate root is responsible for maintaining its own consistency boundary. No external class modifies aggregate internals directly — all changes go through aggregate methods that validate and raise domain events.

---

## Consequences

- Richer aggregate classes compared to anemic models — accepted for explicit business rule location
- Domain events decouple aggregates from side effects (Kafka publishing, notification) — see [ADR-002](ADR-002-kafka.md)
- Ubiquitous language reduces translation overhead between product specs and code
- Anti-corruption layers are needed when integrating external systems (Maps API, Payment Gateway) to prevent their models from leaking into the domain
