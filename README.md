# Smart Logistics Network

A multi-service logistics platform coordinating Shippers, Carriers, Drivers, Warehouse Operators, and Platform Administrators. Built with Java 21, Spring Boot 3, Apache Kafka, and PostgreSQL, following Hexagonal Architecture and Domain-Driven Design.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     Smart Logistics Network                      │
│                                                                  │
│  shipment-service ──┐                                           │
│  fleet-service    ──┤                                           │
│  driver-service   ──┼──► Apache Kafka ──► notification-service  │
│  routing-service  ──┤                 └──► billing-service       │
│  warehouse-service──┘                 └──► (analytics, future)   │
│                                                                  │
│  All services share one PostgreSQL, each in its own schema.     │
└─────────────────────────────────────────────────────────────────┘
```

Each service is a **self-contained Maven module** with:
- A domain layer (aggregates, value objects, domain events, ports) with zero framework dependencies
- An application layer (use case implementations)
- Infrastructure adapters (REST controllers, JPA repositories, Kafka publishers/consumers)
- Its own Flyway-managed PostgreSQL schema
- Its own `module-info.java` (JPMS)

---

## Services

| Service | Port | Schema | Kafka Topics Published |
|---------|------|--------|------------------------|
| shipment-service | 8081 | `shipment` | `shipment.created`, `shipment.assigned`, `shipment.cancelled` |
| fleet-service | 8082 | `fleet` | `fleet.vehicle-registered`, `fleet.vehicle-status-changed` |
| driver-service | 8083 | `driver` | `fleet.driver-registered`, `fleet.driver-status-changed` |
| routing-service | 8084 | `routing` | `routing.route-calculated` |
| warehouse-service | 8085 | `warehouse` | `warehouse.inventory-received`, `warehouse.capacity-updated` |
| billing-service | 8086 | `billing` | `billing.invoice-generated` |
| notification-service | 8087 | `notification` | *(consumes only)* |

---

## Tech Stack

| Concern | Choice |
|---------|--------|
| Language | Java 21 (virtual threads via Project Loom) |
| Framework | Spring Boot 3.3 |
| Messaging | Apache Kafka 7.6 (KRaft mode — no ZooKeeper) |
| Database | PostgreSQL 16 |
| Migrations | Flyway (per-service schema) |
| Module system | JPMS (`module-info.java` per service) |
| Architecture | Hexagonal (Ports & Adapters) |
| Domain modeling | DDD — aggregates, value objects, domain events |
| Error responses | RFC 9457 ProblemDetail |
| Build | Maven multi-module |
| Containers | Docker + Docker Compose |
| Testing | JUnit 5 + Mockito |

---

## Running Locally

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker Desktop

### Start infrastructure

```bash
docker compose up -d postgres kafka
```

### Build and run all services

```bash
mvn clean package -DskipTests
docker compose up --build
```

### Run only infrastructure + start services from IDE

```bash
docker compose up -d postgres kafka
# Then start each service's main class from your IDE
```

### Run tests

```bash
# All tests
mvn test

# Single service
mvn test -pl shipment-service

# Single test class
mvn test -pl shipment-service -Dtest=ShipmentTest
```

---

## Module Structure

```
smart-logistics-network/
├── pom.xml                          # Parent POM (Java 21, Spring Boot BOM)
├── docker-compose.yml               # PostgreSQL 16 + Kafka KRaft + all 7 services
├── common/                          # Shared domain primitives (AggregateRoot, DomainEvent)
├── shipment-service/                # Shipment lifecycle management
├── fleet-service/                   # Vehicle registration and capacity
├── driver-service/                  # Driver management and BR-005 hours tracking
├── routing-service/                 # Route calculation (Haversine → Maps API in Phase 4)
├── warehouse-service/               # Inventory and capacity management
├── billing-service/                 # Invoicing, SLA penalties, carrier payments
├── notification-service/            # Email notifications via Kafka event consumption
└── specs-documentation/             # ADRs, epics, features, acceptance tests, docs
    ├── adrs/                        # ADR-001 through ADR-018
    ├── docs/                        # Overview, templates, code-creation guide
    └── specs/                       # Epics, features, user stories, acceptance tests
```

---

## API Quick Reference

All services return `ProblemDetail` (RFC 9457) on error.

### Shipment Service (`localhost:8081`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/shipments` | Create a new shipment |
| GET | `/api/v1/shipments/{id}` | Get shipment by ID |
| POST | `/api/v1/shipments/{id}/assign` | Assign driver and vehicle |
| POST | `/api/v1/shipments/{id}/cancel` | Cancel a shipment |

### Fleet Service (`localhost:8082`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/vehicles` | Register a vehicle |
| GET | `/api/v1/vehicles/{id}` | Get vehicle by ID |
| GET | `/api/v1/vehicles/available` | List vehicles meeting cargo requirements |
| PATCH | `/api/v1/vehicles/{id}/status` | Update vehicle status |

### Driver Service (`localhost:8083`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/drivers` | Register a driver |
| GET | `/api/v1/drivers/{id}` | Get driver by ID |
| POST | `/api/v1/drivers/{id}/driving-sessions` | Record a driving session |

### Routing Service (`localhost:8084`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/routes/calculate` | Calculate route between two addresses |

### Warehouse Service (`localhost:8085`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/warehouses` | Register a warehouse |
| GET | `/api/v1/warehouses/{id}` | Get warehouse by ID |
| POST | `/api/v1/warehouses/{id}/inventory` | Receive inventory (BR-006) |

### Billing Service (`localhost:8086`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/invoices` | Generate an invoice |
| GET | `/api/v1/invoices/{id}` | Get invoice by ID |
| POST | `/api/v1/invoices/{id}/pay` | Mark invoice as paid |

### Notification Service (`localhost:8087`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/notifications` | Send a notification directly |
| GET | `/api/v1/notifications/{id}` | Get notification status |

---

## Key Business Rules

| Rule | Enforced In |
|------|-------------|
| BR-001/002: Weight/volume ≤ vehicle capacity | `Vehicle.canCarry()` |
| BR-003: Hazmat requires certified driver | `Driver.canDriveHazmat()` |
| BR-004: ETA ≤ promised delivery date | Routing + assignment validation |
| BR-005: Max 9 driving hours/day | `DrivingSession.wouldExceedLimit()` |
| BR-006: Warehouse `currentCapacity + incoming ≤ maxCapacity` | `Warehouse.receiveInventory()` |
| BR-007: Cancellation rules by status | `Shipment.cancel()` |
| BR-008: Cold chain requires refrigerated vehicle | `Vehicle.supportsColdChain()` |

---

## Architecture Decision Records

| ADR | Decision |
|-----|----------|
| [ADR-001](specs-documentation/adrs/ADR-001-java21.md) | Java 21 + virtual threads |
| [ADR-002](specs-documentation/adrs/ADR-002-kafka.md) | Apache Kafka for async domain events |
| [ADR-003](specs-documentation/adrs/ADR-003-docker.md) | Docker + Docker Compose |
| [ADR-004](specs-documentation/adrs/ADR-004-jpms.md) | JPMS for boundary enforcement |
| [ADR-005](specs-documentation/adrs/ADR-005-hexagonal.md) | Hexagonal Architecture |
| [ADR-006](specs-documentation/adrs/ADR-006-ddd.md) | Domain-Driven Design |
| [ADR-007](specs-documentation/adrs/ADR-007-java-records.md) | Java records for value objects and events |
| [ADR-008](specs-documentation/adrs/ADR-008-aggregate-factories.md) | `create()` vs `reconstitute()` factory pattern |
| [ADR-009](specs-documentation/adrs/ADR-009-problem-detail.md) | RFC 9457 ProblemDetail for errors |
| [ADR-010](specs-documentation/adrs/ADR-010-kafka-pattern-matching.md) | Pattern-matching switch for Kafka routing |
| [ADR-011](specs-documentation/adrs/ADR-011-optimistic-locking.md) | Optimistic locking via `@Version` |
| [ADR-012](specs-documentation/adrs/ADR-012-per-service-flyway-schemas.md) | Per-service Flyway schemas |
| [ADR-013](specs-documentation/adrs/ADR-013-haversine-routing.md) | Haversine placeholder routing engine |
| [ADR-014](specs-documentation/adrs/ADR-014-notification-strategy.md) | Strategy pattern for notification senders |
| [ADR-015](specs-documentation/adrs/ADR-015-pull-domain-events.md) | `pullDomainEvents()` clearing semantics |
| [ADR-016](specs-documentation/adrs/ADR-016-driving-sessions-table.md) | Driving sessions as separate table |
| [ADR-017](specs-documentation/adrs/ADR-017-sla-penalty-rates.md) | SLA penalty rates by tier |
| [ADR-018](specs-documentation/adrs/ADR-018-bigdecimal-money.md) | BigDecimal for monetary values |

---

## Roadmap

| Phase | Scope | Status |
|-------|-------|--------|
| 1 | Shipment + Fleet + Driver management | ✅ Implemented |
| 2 | Route Optimization + Warehouse + Billing + Notifications | ✅ Implemented |
| 3 | Event-Driven Architecture + Microservices + CQRS + Transactional Outbox | Planned |
| 4 | AI Forecasting + Dynamic Pricing + Predictive Maintenance + Maps API | Planned |
| 5 | Multi-Tenant SaaS + Global Network + Carrier Marketplace | Planned |
