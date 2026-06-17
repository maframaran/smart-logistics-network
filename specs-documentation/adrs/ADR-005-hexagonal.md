# ADR-005 — Hexagonal Architecture (Ports & Adapters)

**Status:** Accepted

---

## Context

Each service must remain testable, framework-agnostic, and easy to evolve. A traditional layered architecture (Controller → Service → Repository) bleeds framework concerns into business logic: Spring annotations in domain classes, JPA entities as domain objects, Kafka consumers calling business methods directly.

---

## Decision

Apply **Hexagonal Architecture** (Ports & Adapters) to every service.

### Layer Structure

```
┌─────────────────────────────────────────────────┐
│                   Domain                         │
│  Aggregates · Value Objects · Domain Events      │
│  Repository Ports · Use Case Ports               │
│  (zero framework dependencies)                   │
└──────────────────┬──────────────────────────────┘
                   │ implements / calls
┌──────────────────▼──────────────────────────────┐
│             Application (Use Cases)              │
│  Orchestrates domain objects                     │
│  Implements inbound ports                        │
│  Calls outbound ports                            │
└────────┬──────────────────────────┬─────────────┘
         │                          │
┌────────▼────────┐      ┌──────────▼─────────────┐
│ Inbound Adapters│      │   Outbound Adapters     │
│ REST Controller │      │ JPA Repository Impl     │
│ Kafka Consumer  │      │ Kafka Event Publisher   │
│ CLI / Scheduler │      │ REST Client (Maps API)  │
└─────────────────┘      └────────────────────────┘
```

### Port Types
- **Inbound ports** (`UseCase` interfaces): defined in the domain, implemented by application layer. Example: `AssignShipmentUseCase`
- **Outbound ports** (`Repository`, `EventPublisher`, external APIs): defined in the domain, implemented by infrastructure adapters. Example: `ShipmentRepository`, `RouteCalculationPort`

### Package Convention
```
com.logistics.<domain>/
  domain/
    model/          # aggregates, value objects
    events/         # domain events
    ports/
      in/           # inbound use case ports
      out/          # outbound repository/service ports
  application/
    usecases/       # use case implementations
  infrastructure/
    persistence/    # JPA adapters
    messaging/      # Kafka adapters
    rest/           # REST controllers and clients
```

---

## Consequences

- Domain objects are plain Java records/classes — no `@Entity`, no `@Component`
- Use cases can be unit-tested without a Spring context, database, or Kafka broker
- Swapping persistence (e.g. PostgreSQL → MongoDB for a specific domain) only changes one adapter
- Enforced at compile time by JPMS: domain module cannot `requires` infrastructure modules (see [ADR-004](ADR-004-jpms.md))
- Slightly more boilerplate upfront (interfaces for every port) — accepted trade-off for long-term maintainability
