# Smart Logistics Network

A multi-service logistics platform coordinating Shippers, Carriers, Drivers, Warehouse Operators, and Platform Administrators. Built with Java 21, Spring Boot 3, Apache Kafka, and PostgreSQL, following Hexagonal Architecture and Domain-Driven Design.

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                      Smart Logistics Network                          │
│                                                                       │
│  Browser ──► logistics-ui:3000 (Next.js BFF)                        │
│               │                                                       │
│               ├──► shipment-service:8081 ──┐                         │
│               ├──► fleet-service:8082    ──┤                         │
│               ├──► driver-service:8083   ──┼──► Kafka ──► notification-service │
│               ├──► routing-service:8084  ──┤         └──► billing-service      │
│               ├──► warehouse-service:8085──┘                         │
│               ├──► billing-service:8086                              │
│               ├──► notification-service:8087                         │
│               └──► rag-service:8088 (RAG Intelligence)               │
│                                                                       │
│  All backend services share one PostgreSQL, each in its own schema.  │
└──────────────────────────────────────────────────────────────────────┘
```

Each service is a **self-contained Maven module** with:
- A domain layer (aggregates, value objects, domain events, ports) with zero framework dependencies
- An application layer (use case implementations)
- Infrastructure adapters (REST controllers, JPA repositories, Kafka publishers/consumers)
- Its own Flyway-managed PostgreSQL schema
- Its own `module-info.java` (JPMS)

---

## Services

| Service | Port | Schema | Kafka Topics Published | API Docs |
|---------|------|--------|------------------------|----------|
| shipment-service | 8081 | `shipment` | `shipment.created`, `shipment.assigned`, `shipment.cancelled` | [localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) |
| fleet-service | 8082 | `fleet` | `fleet.vehicle-registered`, `fleet.vehicle-status-changed` | [localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) |
| driver-service | 8083 | `driver` | `fleet.driver-registered`, `fleet.driver-status-changed` | [localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html) |
| routing-service | 8084 | `routing` | `routing.route-calculated` | [localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html) |
| warehouse-service | 8085 | `warehouse` | `warehouse.inventory-received`, `warehouse.capacity-updated` | [localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html) |
| billing-service | 8086 | `billing` | `billing.invoice-generated` | [localhost:8086/swagger-ui.html](http://localhost:8086/swagger-ui.html) |
| notification-service | 8087 | `notification` | *(consumes only)* | [localhost:8087/swagger-ui.html](http://localhost:8087/swagger-ui.html) |
| rag-service | 8088 | `rag` | *(consumes only — pgvector + Claude API)* | [localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html) |

Each service also exposes its raw OpenAPI 3 spec at `/v3/api-docs` ([ADR-029](specs-documentation/adrs/ADR-029-openapi-springdoc.md)).

---

## Tech Stack

**Backend**

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
| Testing | JUnit 5 + Mockito + Cucumber (acceptance-tests module) |
| Vector store | pgvector (PostgreSQL extension, `rag` schema, IVFFlat ANN) |
| AI / LLM | Claude API — `claude-haiku-4-5-20251001` (embeddings) · `claude-sonnet-4-6` (completions) |

**Frontend (logistics-ui)**

| Concern | Choice |
|---------|--------|
| Framework | Next.js 15 (App Router) |
| Language | TypeScript |
| Styling | Tailwind CSS + shadcn/ui |
| Server state | TanStack Query v5 |
| Auth | Auth.js v5 (JWT, httpOnly cookie) |
| Charts | Recharts |
| BFF | Next.js API Routes proxy to all 7 services |

---

## Deployment Guide

### Prerequisites

| Tool | Minimum version | Purpose |
|------|----------------|---------|
| Java | 21 | Build backend services |
| Maven | 3.9 | Build tool |
| Docker Desktop | 4.x | Run all services in containers |
| Node.js | 22 | (Optional) Run logistics-ui outside Docker |

### 1. Clone the repository

```bash
git clone <repo-url>
cd smart-logistics-network
```

### 2. Set environment variables

Create a `.env` file at the project root (never commit this file):

```bash
# Required for RAG intelligence features
ANTHROPIC_API_KEY=sk-ant-...

# Next.js auth secret — change for every environment
NEXTAUTH_SECRET=change-me-in-production
```

Docker Compose reads `.env` automatically. If you skip this step, RAG endpoints
will still respond but will return hash-based (non-semantic) embeddings and empty
LLM results.

### 3. Build all Java services

```bash
mvn clean package -DskipTests
```

This compiles all 12 Maven modules and produces a fat JAR for each service.
The `logistics-ui` Docker image is built by Docker at step 4 (no Maven needed).

### 4. Start the full stack

```bash
docker compose up --build
```

Docker Compose will:
1. Start **PostgreSQL 16** and **Kafka** (KRaft mode, no ZooKeeper)
2. Wait for both to be healthy
3. Build and start all 9 backend service images in parallel
4. Build and start the **Next.js** customer portal

First run takes ~3–5 minutes while Docker builds all images and downloads layers.
Subsequent runs skip the build cache and start in ~30 seconds.

### 5. Verify all services are up

```bash
# Quick health check for every backend service
for port in 8081 8082 8083 8084 8085 8086 8087 8088; do
  echo -n "Port $port: "
  curl -s http://localhost:$port/actuator/health | python3 -m json.tool 2>/dev/null | grep status || echo "unreachable"
done

# Customer portal
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000
```

All services should report `"status": "UP"`. The portal should return `200`.

### 6. Open the customer portal

Navigate to [http://localhost:3000](http://localhost:3000).

| Role | Email | Password |
|------|-------|----------|
| Shipper | `shipper@platform.local` | `shipper123` |
| Carrier | `carrier@platform.local` | `carrier123` |

### 7. (Optional) Run acceptance tests against the live stack

```bash
# All 22 Cucumber scenarios — requires the stack from step 4 to be running
mvn verify -pl acceptance-tests

# Subsets
mvn verify -pl acceptance-tests -Dgroups="not @ui"   # 12 backend API scenarios
mvn verify -pl acceptance-tests -Dgroups="@ui"        # 5 Playwright UI scenarios
mvn verify -pl acceptance-tests -Dgroups="@rag"       # 5 RAG intelligence scenarios
```

Override service URLs if running against a remote stack:

```bash
SHIPMENT_SERVICE_URL=http://my-host:8081 \
ANTHROPIC_API_KEY=sk-ant-... \
mvn verify -pl acceptance-tests
```

### Stopping the stack

```bash
docker compose down          # stop containers, keep volumes (data survives)
docker compose down -v       # stop containers and delete volumes (clean slate)
```

---

## Running Locally (Development Mode)

Run only infrastructure in Docker and start services directly from your IDE or terminal.

### Start infrastructure only

```bash
docker compose up -d postgres kafka
```

### Run a single service

```bash
# Example: shipment-service on its default port
mvn spring-boot:run -pl shipment-service
```

### Run tests

```bash
# All unit tests
mvn test

# Single service
mvn test -pl shipment-service

# Single test class
mvn test -pl shipment-service -Dtest=ShipmentTest

# Acceptance tests (requires docker compose up -d first)
mvn verify -pl acceptance-tests                          # all scenarios
mvn verify -pl acceptance-tests -Dgroups="not @ui"       # backend API scenarios only
mvn verify -pl acceptance-tests -Dgroups="@ui"           # Playwright UI scenarios only
mvn verify -pl acceptance-tests -Dgroups="@rag"          # RAG intelligence scenarios only
```

---

## Module Structure

```
smart-logistics-network/
├── pom.xml                          # Parent POM (Java 21, Spring Boot BOM, 12 modules)
├── docker-compose.yml               # PostgreSQL 16 + Kafka KRaft + 7 services + logistics-ui
├── common/                          # Shared domain primitives (AggregateRoot, DomainEvent)
├── shipment-service/                # Shipment lifecycle management
├── fleet-service/                   # Vehicle registration and capacity
├── driver-service/                  # Driver management and BR-005 hours tracking
├── routing-service/                 # Route calculation (Haversine → Maps API in Phase 4)
├── warehouse-service/               # Inventory and capacity management
├── billing-service/                 # Invoicing, SLA penalties, carrier payments
├── notification-service/            # Email notifications via Kafka event consumption
├── acceptance-tests/                # Cucumber acceptance tests (backend + UI + RAG), runs via mvn verify
│   └── src/test/java/com/logistics/tests/acceptance/
│       ├── AcceptanceTestBase.java  # Env-var service URLs, no Spring context
│       ├── KafkaTestHelper.java     # pollUntilKey() for Kafka event assertions
│       ├── BackendAcceptanceRunner.java  # Cucumber runner — API scenarios (not @ui)
│       ├── UiAcceptanceRunner.java       # Cucumber runner — Playwright scenarios (@ui)
│       └── stepdefinitions/         # 22 step definition classes (17 backend/UI + 5 RAG)
├── logistics-ui/                    # Next.js 15 customer portal (Shipper + Carrier)
│   ├── app/                         # App Router pages (login, dashboard, shipments, fleet, warehouse, billing)
│   ├── components/                  # shadcn/ui primitives + domain components
│   ├── lib/                         # BFF proxy, TanStack Query client, typed API libs
│   └── types/                       # TypeScript interfaces matching backend JSON
├── rag-service/                     # RAG Intelligence — pgvector + Claude API (port 8088)
│   ├── domain/                      # EmbeddingPort, LlmPort, VectorStorePort + 5 result models
│   ├── application/                 # RouteSearch, WaiverAssistant, PricingAdvisor, InventoryAdvisor, DemandForecast
│   └── infrastructure/              # ClaudeEmbeddingAdapter, ClaudeLlmAdapter, PgVectorStoreAdapter, RagKafkaConsumer, RagController
└── specs-documentation/             # ADRs, epics, features, acceptance tests, docs
    ├── adrs/                        # ADR-001 through ADR-029
    ├── docs/                        # Overview, templates, code-creation guide
    ├── services/                    # Per-service descriptors (ports, env vars, endpoints)
    │   └── rag-service/service.md   # rag-service descriptor
    ├── specs/                       # Epics (EP-001–EP-018), features, user stories
    │   └── acceptance-tests/        # 22 Gherkin .feature files (source of truth)
    └── tests/
        ├── e2e/                     # Placeholder — full cross-service E2E (Phase 3)
        └── contract/                # Placeholder — contract tests (Phase 3)
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

### RAG Service (`localhost:8088`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/rag/routes/similar` | Route similarity + cost/ETA estimate (F-026) |
| POST | `/api/v1/rag/invoices/{id}/waiver` | SLA waiver recommendation (F-027) |
| POST | `/api/v1/rag/pricing/recommend` | Dynamic pricing recommendation (F-028) |
| GET | `/api/v1/rag/warehouses/{id}/rebalance` | Inventory rebalancing advice (F-029) |
| GET | `/api/v1/rag/forecast` | Shipment demand forecast (F-030) |

Requires `ANTHROPIC_API_KEY` environment variable. See [service descriptor](specs-documentation/services/rag-service/service.md) and [ADR-024](specs-documentation/adrs/ADR-024-rag-pgvector.md).

### Customer Portal (`localhost:3000`)

| Page | Role | Description |
|------|------|-------------|
| `/login` | All | Auth.js credentials login |
| `/dashboard` | All | Role-scoped stat cards |
| `/shipments` | Shipper | Shipment list with status filter tabs (15s auto-refresh) |
| `/shipments/[id]` | Shipper | Detail with status timeline, cargo, assignment, route |
| `/fleet` | Carrier | Vehicle and driver cards |
| `/warehouse` | Carrier | Capacity gauges per warehouse |
| `/warehouse/[id]` | Carrier | Inventory table with expiration highlighting |
| `/billing` | Shipper | Invoice table with SLA penalty highlighting |
| `/billing/[id]` | Shipper | Invoice detail with line items and carrier payment |

Demo credentials: `shipper@platform.local` / `shipper123` · `carrier@platform.local` / `carrier123`

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
| [ADR-019](specs-documentation/adrs/ADR-019-nextjs.md) | Next.js 15 App Router for the customer portal |
| [ADR-020](specs-documentation/adrs/ADR-020-bff-pattern.md) | BFF pattern via Next.js API Routes |
| [ADR-021](specs-documentation/adrs/ADR-021-shadcn-tailwind.md) | shadcn/ui + Tailwind CSS |
| [ADR-022](specs-documentation/adrs/ADR-022-tanstack-query.md) | TanStack Query v5 for server state |
| [ADR-023](specs-documentation/adrs/ADR-023-authjs.md) | Auth.js v5 for session management |
| [ADR-024](specs-documentation/adrs/ADR-024-rag-pgvector.md) | pgvector + Claude API for RAG intelligence |
| [ADR-025](specs-documentation/adrs/ADR-025-spring-data-jpa-standard.md) | Spring Data JPA as the standard persistence mechanism |
| [ADR-026](specs-documentation/adrs/ADR-026-typed-records-over-maps.md) | Typed records over `Map<String, Object>` at structured-data boundaries |
| [ADR-027](specs-documentation/adrs/ADR-027-kafka-topic-config-dispatch.md) | Map-based Kafka topic dispatch with externalized topic names |
| [ADR-028](specs-documentation/adrs/ADR-028-lombok-builder-getter.md) | Lombok `@Builder`/`@Getter` for pure-assignment aggregate constructors |
| [ADR-029](specs-documentation/adrs/ADR-029-openapi-springdoc.md) | OpenAPI 3 documentation via springdoc |

---

## Roadmap

| Phase | Scope | Status |
|-------|-------|--------|
| 1 | Shipment + Fleet + Driver management | ✅ Implemented |
| 2 | Route Optimization + Warehouse + Billing + Notifications | ✅ Implemented |
| UI | Customer portal (Shipper + Carrier) — Next.js 15 + Auth.js + TanStack Query | ✅ Implemented |
| Tests | Acceptance-tests Maven module — 22 Cucumber scenarios (12 backend + 5 UI + 5 RAG) | ✅ Implemented |
| RAG | rag-service — pgvector + Claude API, 5 endpoints, Kafka consumer, 22 Cucumber scenarios | ✅ Implemented |
| 3 | Event-Driven Architecture (Transactional Outbox, DLQ, Avro) + Microservices (OpenAPI, OTel, Prometheus) + CQRS read models | 🟡 Planned — Prometheus dependency present in 7/8 services but not instrumented; everything else not started. See [Phase 3–5 implementation plan](#phase-35-implementation-plan) below. |
| 4 | AI Forecasting + Dynamic Pricing + Predictive Maintenance + Maps API | 🟡 Partially implemented — AI Forecasting and Dynamic Pricing fully shipped via `rag-service` (`DemandForecastService`, `PricingAdvisorService`); Predictive Maintenance and Maps API not started. See plan below. |
| 5 | Multi-Tenant SaaS + Global Network + Carrier Marketplace | Planned — see plan below |

### Phase 3–5 Implementation Plan

The work below is sequenced by **dependency, not phase number** — e.g. the Transactional Outbox/DLQ fix is a cross-cutting correctness fix that later Kafka consumers (predictive maintenance, CQRS projectors, marketplace) are built on top of, not before. Four scope decisions are locked in: **Maps API uses OSRM self-hosted via Docker** (free, no API key); **Predictive Maintenance is LLM-based**, extending `rag-service`'s existing `LlmPort`/`ClaudeLlmAdapter` pattern rather than a new service; **observability gets a real local backend** (Prometheus + Grafana + Jaeger via docker-compose, not just instrumented-into-a-void); **Phase 5 is included in full**, with explicit flagged assumptions since no tenancy or marketplace design has been validated yet (see the open-assumptions list at the end).

| Stage | Scope | New ADR(s) | Depends on |
|-------|-------|------------|------------|
| 0 | ADR stubs + decompose EP-007–EP-015 into feature specs | — | — |
| 1 | ✅ OpenAPI 3 docs across all 8 REST controllers (springdoc) | ADR-029 | — |
| 2 | **Transactional Outbox + DLQ** — fixes a real gap: `KafkaTemplate.send()` is non-blocking, so a broker failure after a use case's `@Transactional` method returns can silently lose an already-"published" domain event today. Outbox table written atomically with the aggregate inside the existing repository `save()`; `OutboxRelayScheduler` publishes async. DLQ via `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` on every `@KafkaListener` (notification-service's consumer currently has zero error handling — most urgent fix). | ADR-030, ADR-031 | — |
| 3 | Avro + Schema Registry (new `schema-registry` container; keep existing typed-record DTO convention, map Avro↔record at the infra boundary) | ADR-032 | Stage 2 |
| 4 | OSRM self-hosted routing (new `osrm` container, São Paulo–Rio OSM extract baked in; Haversine kept as a profile-gated fallback) | ADR-033 | — |
| 5 | Predictive Maintenance — new `PredictiveMaintenanceService` inside `rag-service` (4th LLM-advisor service, same shape as `DemandForecastService`); `Vehicle` gains a `mileage` field as a telemetry stand-in (no real IoT data exists) | ADR-034 | Stage 2 |
| 6 | CQRS read model — denormalized `ShipmentTrackingView` + idempotent-upsert projector, sub-50ms tracking queries | ADR-035 | Stages 2, 3 |
| 7 | **Multi-Tenant SaaS** — row-level `tenant_id` + Hibernate filter (not schema-per-tenant), JWT claim → `X-Tenant-Id` header → `ThreadLocal` context propagation. Highest-risk stage — see open assumptions below. | ADR-036 | — |
| 8 | Carrier Marketplace — new `marketplace-service` module (`Load`/`Bid` aggregates, weighted price+rating matching); Global Network/customs scoped to a placeholder stub only (most underspecified item in the plan) | ADR-037, ADR-038 | Stage 7 |
| 9 | Observability backend — Prometheus + Grafana + Jaeger added to docker-compose; OTel tracing wired into all 9 services; first real Micrometer metrics (outbox relay lag, projector lag, LLM call latency) replacing the currently-aspirational convention documented in `code-creation-guide.md` | ADR-039 | All prior stages with something worth measuring |
| 10 | README + specs-documentation finalization — flip Roadmap statuses to Implemented, finalize all ADRs, update topic docs | — | All |

**Open assumptions in Stage 7/8 requiring validation before/while implementing** (flagged, not yet confirmed against real requirements):
1. Row-level `tenant_id` + Hibernate filter, not schema- or database-per-tenant.
2. Tenant registry folded into an existing service rather than a new module — revisit if tenant lifecycle (billing, suspension, custom domains) grows complex.
3. Marketplace matching is a simple explainable weighted score (price + rating), not ML-based, for v1.
4. Carrier rating is a stub (flat default) — no real rating subsystem exists yet.
5. Global Network/customs is a placeholder stub only — needs real product input before deeper investment.

Full per-stage file lists, exact patterns to follow, and verification steps are tracked in the working implementation plan (ask to resume Phase 3–5 work to pick up at any stage).
