# End-to-End Tests

Multi-service scenario tests that exercise full cross-service flows.

---

## Status

**Deferred to Phase 3** — E2E tests require all Phase 1 & 2 services to be built and running. They are defined here now so the structure is in place.

---

## What E2E Tests Cover

Full business flows that span multiple services via Kafka:

| Scenario | Services involved |
|----------|------------------|
| Shipment created → route calculated → assigned → delivered → invoice generated | shipment, routing, billing, notification |
| Vehicle breakdown → reassignment | shipment, fleet |
| Warehouse overflow → alternative suggested | warehouse, shipment |

---

## Infrastructure

E2E tests use Docker Compose to start all services + Kafka + PostgreSQL:

```bash
docker compose -f docker-compose.test.yml up -d
./mvnw test -pl tests/e2e
docker compose -f docker-compose.test.yml down
```

---

## Naming Convention

| Scope | Suffix | Example |
|-------|--------|---------|
| E2E test | `E2ETest` | `ShipmentLifecycleE2ETest` |

---

## When to Add an E2E Test

Add an E2E test when:
1. A user story spans more than one service (e.g. US-001 + US-038: shipper creates shipment → carrier gets paid)
2. A Kafka event chain needs to be validated end-to-end
3. A failure scenario (vehicle breakdown, warehouse overflow) needs full-flow verification
