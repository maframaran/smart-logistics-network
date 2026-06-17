# Integration Tests

Per-service integration tests using Spring Boot Test + Testcontainers.

---

## What Integration Tests Cover

Each integration test boots the **full application context** of one service with real infrastructure:
- Real PostgreSQL (Testcontainers)
- Real Kafka (Testcontainers, KRaft mode)
- Real Spring Boot context (`@SpringBootTest`)

They test a single service's adapter stack end-to-end without mocking any infrastructure:

```
HTTP request (REST Assured in-process)
  → Spring REST controller
    → Use case
      → Domain aggregate (business rules enforced)
        → JPA adapter → real PostgreSQL
        → Kafka publisher → real Kafka topic
```

---

## What Integration Tests Do NOT Cover

- Cross-service scenarios (that's `e2e/`)
- Business rule logic (that's unit tests on the aggregate)
- Gherkin scenarios (that's `acceptance/`)

---

## Template

See `templates/ShipmentServiceIntegrationTest.java` for a full example.

Key patterns:
1. Extend `IntegrationTestBase` (starts containers, wires Spring context)
2. Use `RestAssured` to make HTTP calls against the started server
3. Assert database state via a Spring `JdbcTemplate` or repository
4. Assert Kafka messages via `KafkaTestHelper.pollUntilKey(topic, key)`

---

## Naming Convention

| Scope | Suffix | Example |
|-------|--------|---------|
| Integration test | `IT` | `CreateShipmentIT` |
| Helper/base class | `Base` or `Helper` | `IntegrationTestBase` |
