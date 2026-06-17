# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Status

Phases 1, 2, the customer portal (UI), and the RAG intelligence service are fully implemented. The codebase contains 12 Maven modules (common + 7 services + acceptance-tests + logistics-ui + rag-service), 67 passing unit tests, 22 Cucumber acceptance scenarios (12 backend + 5 UI + 5 RAG), and a Docker Compose stack that starts all services including the Next.js portal.

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
rag-service/             # RAG Intelligence — pgvector + Claude API (port 8088); fully implemented
  Consumes: routing.route-calculated, billing.invoice-generated, warehouse.capacity-updated, shipment.created
  Endpoints: /api/v1/rag/routes/similar, /waiver, /pricing/recommend, /warehouses/{id}/rebalance, /forecast
  Requires: ANTHROPIC_API_KEY env var (never commit)
acceptance-tests/        # Cucumber acceptance tests — runs via mvn verify (not mvn test)
  src/test/java/com/logistics/tests/acceptance/
    AcceptanceTestBase.java        # Env-var service URLs, no Spring context
    KafkaTestHelper.java           # pollUntilKey() for Kafka event assertions
    BackendAcceptanceRunner.java   # Runs 12 API scenarios (tag: not @ui)
    UiAcceptanceRunner.java        # Runs 5 Playwright UI scenarios (tag: @ui)
    stepdefinitions/               # 22 step definition classes (17 backend/UI + 5 RAG tagged @rag)
logistics-ui/            # Next.js 15 customer portal (Shipper + Carrier roles)
  app/                   # App Router — login, dashboard, shipments, fleet, warehouse, billing
  components/            # shadcn/ui primitives + StatusBadge, VehicleCard, CapacityGauge, etc.
  lib/                   # bff.ts proxy, TanStack Query client, typed API libs per service
  types/                 # TypeScript interfaces matching backend JSON (shipment, vehicle, etc.)
specs-documentation/     # ADRs, feature specs, acceptance tests, docs
  adrs/                  # ADR-001 through ADR-024 (ADR-024: pgvector + Claude API)
  docs/                  # Overview, code-creation guide, templates
  services/              # Per-service descriptors (ports, env vars, endpoints)
    rag-service/         # service.md — full rag-service contract
  specs/                 # Epics (EP-001–EP-018), features, user stories
    acceptance-tests/    # 22 Gherkin .feature files — source of truth for Cucumber
  tests/
    e2e/                 # Placeholder — cross-service E2E flows (Phase 3)
    contract/            # Placeholder — contract tests (Phase 3)
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

# Start all services + infrastructure + UI
docker compose up --build

# Check schemas after Flyway migrations
docker compose exec postgres psql -U logistics -d logistics -c "\dn"

# Run acceptance tests (requires docker compose up -d first)
mvn verify -pl acceptance-tests                        # all scenarios
mvn verify -pl acceptance-tests -Dgroups="not @ui"     # backend API only
mvn verify -pl acceptance-tests -Dgroups="@ui"         # Playwright UI only
mvn verify -pl acceptance-tests -Dgroups="@rag"        # RAG intelligence only
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
| logistics-ui | 3000 |
| shipment-service | 8081 |
| fleet-service | 8082 |
| driver-service | 8083 |
| routing-service | 8084 |
| warehouse-service | 8085 |
| billing-service | 8086 |
| notification-service | 8087 |
| rag-service | 8088 |
| PostgreSQL | 5432 |
| Kafka | 9092 |

## Event-Driven Integration

Services communicate via domain events on Kafka. Key flows:
- `ShipmentCreated` → notification-service (email to shipper) + rag-service (demand forecast index)
- `ShipmentAssigned` → notification-service (email to driver)
- `ShipmentDelivered` → notification-service (email to shipper/customer)
- `ShipmentCancelled` → notification-service
- `RouteCalculated` → rag-service (route similarity index)
- `InvoiceGenerated` → rag-service (waiver + pricing index)
- `CapacityUpdated` → rag-service (inventory advisor index)

## RAG Intelligence (rag-service) Key Facts

- Port `8088`; `rag` schema in shared PostgreSQL; pgvector extension required
- `ANTHROPIC_API_KEY` must be injected via env var — never committed to source control
- Embedding model: `claude-haiku-4-5-20251001` (1536 dimensions)
- Completion model: `claude-sonnet-4-6` via `tool_use` for structured JSON
- ANN query pattern: `ORDER BY embedding <=> $1::vector LIMIT 5` (cosine distance)
- IVFFlat index per table with `lists=50`
- Waiver confidence < 0.6 always returns `ESCALATE` regardless of LLM output
- Dynamic pricing cap: `suggestedPriceBrl ≤ 1.5 × staticRate`; only PAID invoices as comparables
- Demand forecast gives +20% bonus when `targetMonth` shares calendar month with a historical comparable

## logistics-ui Key Patterns

- **BFF proxy**: `lib/bff.ts` `proxyToService()` — all 7 service routes under `app/api/<resource>/[...path]/route.ts`. Browser never calls services directly.
- **Auth**: `auth.ts` + `middleware.ts` — Auth.js v5, JWT in httpOnly cookie, roles `SHIPPER` / `CARRIER`. Demo: `shipper@platform.local/shipper123`, `carrier@platform.local/carrier123`.
- **Data fetching**: Server Components fetch directly from services (revalidate 15–60s). Client Components use TanStack Query with `refetchInterval`.
- **`data-testid` attributes**: All interactive elements carry `data-testid` for Playwright step definitions.

## Acceptance Tests Key Patterns

- `AcceptanceTestBase` has no `@SpringBootTest` — tests point at a live stack via env-var URLs defaulting to `localhost:808x`.
- `KafkaTestHelper.pollUntilKey(topic, key)` polls with `auto.offset.reset=earliest` for up to 5s.
- Backend step defs extend `AcceptanceTestBase` and call service-specific methods (`shipmentUrl()`, `fleetUrl()`, etc.).
- UI step defs use Playwright directly (`Playwright.create()`, `browser.newContext()`, `page`).
- `.feature` files live in `specs-documentation/specs/acceptance-tests/` (documentation source of truth); the Maven module reads them from the project root path.
- UI features are tagged `@ui`; backend features have no tag. Use `not @ui` / `@ui` to run subsets.

## ADRs

All architectural and implementation decisions are documented in `specs-documentation/adrs/`. ADR-001–006: foundational (Java 21, Kafka, Docker, JPMS, Hexagonal, DDD). ADR-007–018: Phase 1/2 implementation decisions. ADR-019–023: UI decisions (Next.js, BFF, shadcn/ui, TanStack Query, Auth.js).

## Spec Conventions

New feature specs follow `specs-documentation/docs/feature-spec-template.md`: Goal, Actors, Preconditions, Workflow, Business Rules, Edge Cases, Acceptance Criteria, Telemetry.

Acceptance criteria use Gherkin (Given/When/Then) as shown in `specs-documentation/docs/overview.md`.

## Roadmap Phases

1. ✅ Shipment + Fleet + Driver Management
2. ✅ Route Optimization + Warehouse + Billing + Notifications
- ✅ Customer portal (logistics-ui) — Next.js 15, Shipper + Carrier roles
- ✅ Acceptance-tests module — 17 Cucumber scenarios, Playwright UI tests
3. Event-Driven Architecture (Transactional Outbox, DLQ, Avro/Schema Registry) + Microservices (OpenAPI, OpenTelemetry, Prometheus) + CQRS read models + E2E tests
4. AI Forecasting + Dynamic Pricing + Predictive Maintenance + Maps API
5. Multi-Tenant SaaS + Global Network + Carrier Marketplace
