# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Status

Phases 1 and 2 are fully implemented. The codebase contains 9 Maven modules (common + 7 services), 67 passing unit tests, and a Docker Compose stack for local development.

## Repository Structure

```
common/                  # AggregateRoot, DomainEvent interface — shared by all services
shipment-service/        # Shipment lifecycle (DRAFT → DELIVERED)
fleet-service/           # Vehicle registration and capacity
driver-service/          # Driver management and BR-005 hour tracking
routing-service/         # Route calculation (Haversine placeholder, port for Phase 4)
warehouse-service/       # Inventory management and BR-006 capacity
billing-service/         # Invoicing, SLA penalties (BRL 50/150/300/day), carrier payments
notification-service/    # Email delivery via Kafka event consumption
specs-documentation/     # ADRs, feature specs, acceptance tests, docs
  adrs/                  # ADR-001 through ADR-018
  docs/                  # Overview, code-creation guide, templates
  specs/                 # Epics, features, user stories, acceptance tests (Gherkin)
```

## Build & Test Commands

```bash
# Build everything
mvn clean package

# Run all tests
mvn test

# Run tests for one service
mvn test -pl shipment-service

# Run a single test class
mvn test -pl billing-service -Dtest=InvoiceTest

# Skip tests during build
mvn clean package -DskipTests

# Start infrastructure (PostgreSQL + Kafka)
docker compose up -d postgres kafka

# Start all services + infrastructure
docker compose up --build

# Check schemas after Flyway migrations
docker compose exec postgres psql -U logistics -d logistics -c "\dn"
```

## Package Convention (Hexagonal Architecture)

Every service follows this exact layout (no exceptions):

```
com.logistics.<domain>/
  domain/
    model/           # Aggregate root + value objects (Java records where immutable)
    events/          # Domain events (Java records implementing DomainEvent)
    ports/
      in/            # UseCase interfaces (inbound ports)
      out/           # Repository + EventPublisher interfaces (outbound ports)
  application/
    usecases/        # UseCase implementations — orchestrate domain + ports
  infrastructure/
    persistence/     # JpaEntity, JpaRepository (outbound adapter)
    messaging/       # KafkaPublisher (outbound) + KafkaConsumer (inbound)
    rest/            # Spring @RestController (inbound adapter)
```

Domain classes must have **zero** `import org.springframework.*` or `import jakarta.persistence.*` statements.

## Key Implementation Patterns

### Aggregate factories (`create()` vs `reconstitute()`)
- `create()` — validates, generates UUID ID, calls `registerEvent()`. Use in application use cases.
- `reconstitute()` — skips validation, no event. Use in JPA repository adapters when mapping from DB.
- Constructor is always `private`.

### Domain events (`pullDomainEvents()`)
- `AggregateRoot.pullDomainEvents()` returns and **clears** the internal event list in one call.
- Publication flow: `repository.save(aggregate)` → `aggregate.pullDomainEvents()` → `publisher.publish(events)`.
- Calling `pullDomainEvents()` twice: first call returns events, second returns empty list.

### Kafka topic routing
- Use Java 21 pattern-matching switch in publisher classes.
- Always include a `default` branch that throws `IllegalArgumentException` — catches unmapped event types immediately.

### Error responses
- All services have `GlobalExceptionHandler` (`@RestControllerAdvice`) returning `ProblemDetail` (RFC 9457).
- Map: `IllegalArgumentException` → 400, `IllegalStateException` → 422, not-found → 404.

### Optimistic locking
- Every JPA entity has `@Version Long version`.
- Every Flyway migration includes `version BIGINT NOT NULL DEFAULT 0` in `CREATE TABLE`.

### Money
- All monetary values in `billing-service` use the `Money` record with `BigDecimal` and scale 2 `HALF_UP`.
- Never use `double` or `float` for amounts.

## JPMS Module Names

Module names in `module-info.java` must match the jar's `Automatic-Module-Name` in `META-INF/MANIFEST.MF`, not the Maven artifact ID. To discover the correct name:

```bash
unzip -p ~/.m2/repository/org/springframework/<artifact>/<version>/<artifact>-<version>.jar \
  META-INF/MANIFEST.MF | grep Automatic-Module-Name
```

Known non-obvious mappings:

| Artifact | `requires` name |
|----------|-----------------|
| `spring-webmvc` | `spring.webmvc` |
| `spring-context-support` | `spring.context.support` |
| `spring-messaging` | `spring.messaging` |
| `spring-kafka` | `spring.kafka` |

If a class is in an **unnamed module** (no `Automatic-Module-Name`), you cannot use it as a parameter type in a JPMS-module class. Workaround: use Spring Messaging annotations (`@Payload`, `@Header`) instead of `ConsumerRecord<>` directly in Kafka listeners.

## Domain Model

**Shipment lifecycle:** `DRAFT → CREATED → SCHEDULED → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED` (also: `CANCELLED`, `FAILED`, `RETURNED`)

**Vehicle status:** `AVAILABLE / ASSIGNED / MAINTENANCE / OUT_OF_SERVICE`

**Driver status:** `AVAILABLE / DRIVING / RESTING / SUSPENDED`

**SLA tiers:** `STANDARD / PRIORITY / EXPRESS`

## Business Rules (with Code Locations)

| Rule | Location |
|------|----------|
| BR-001/002: Weight/volume ≤ vehicle capacity | `fleet-service` `Vehicle.canCarry()` |
| BR-003: Hazmat requires certified driver | `driver-service` `Driver.canDriveHazmat()` |
| BR-004: ETA ≤ promised delivery date | `routing-service` + assignment validation |
| BR-005: Max 9h driving/day | `driver-service` `DrivingSession.wouldExceedLimit()` |
| BR-006: Warehouse capacity | `warehouse-service` `Warehouse.receiveInventory()` |
| BR-007: Cancellation by status | `shipment-service` `Shipment.cancel()` |
| BR-008: Cold chain → refrigerated vehicle | `fleet-service` `Vehicle.supportsColdChain()` |

## Service Ports

| Service | Port |
|---------|------|
| shipment-service | 8081 |
| fleet-service | 8082 |
| driver-service | 8083 |
| routing-service | 8084 |
| warehouse-service | 8085 |
| billing-service | 8086 |
| notification-service | 8087 |
| PostgreSQL | 5432 |
| Kafka | 9092 |

## Event-Driven Integration

Services communicate via domain events on Kafka. Key flows:
- `ShipmentCreated` → notification-service (email to shipper)
- `ShipmentAssigned` → notification-service (email to driver)
- `ShipmentDelivered` → notification-service (email to shipper/customer)
- `ShipmentCancelled` → notification-service

## ADRs

All architectural and implementation decisions are documented in `specs-documentation/adrs/`. ADR-001 through ADR-006 cover foundational choices (Java 21, Kafka, Docker, JPMS, Hexagonal, DDD). ADR-007 through ADR-018 capture implementation decisions made during Phase 1/2.

## Spec Conventions

New feature specs follow `specs-documentation/docs/feature-spec-template.md`: Goal, Actors, Preconditions, Workflow, Business Rules, Edge Cases, Acceptance Criteria, Telemetry.

Acceptance criteria use Gherkin (Given/When/Then) as shown in `specs-documentation/docs/overview.md`.

## Roadmap Phases

1. ✅ Shipment + Fleet + Driver Management
2. ✅ Route Optimization + Warehouse + Billing + Notifications
3. Event-Driven Architecture + Microservices + CQRS + Transactional Outbox
4. AI Forecasting + Dynamic Pricing + Predictive Maintenance + Maps API
5. Multi-Tenant SaaS + Global Network + Carrier Marketplace
