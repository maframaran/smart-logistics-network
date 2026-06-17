# Acceptance Tests

Cucumber + JUnit 5 acceptance tests for the Smart Logistics Network.

---

## How Feature Files Are Resolved

The `.feature` files live in `specs-documentation/specs/acceptance-tests/` and are **product-owned** — scenarios are defined there and must not be modified here. Step definitions implement the scenarios; the spec files drive what needs to be implemented.

The Cucumber runner is configured to point at that directory:

```java
@CucumberOptions(
    features = "specs-documentation/specs/acceptance-tests",
    glue = "com.logistics.tests.acceptance.stepdefinitions"
)
```

This means:
- Adding a new `.feature` file in `specs/acceptance-tests/` automatically creates a pending test
- Running the suite with no step definitions for a new feature will report it as `Undefined` (not failed) — a clear signal to add step definitions

---

## What Each Test Covers

Acceptance tests verify the **full service stack** end-to-end:

```
Gherkin scenario
    → Step definition (Java)
        → Spring Boot application context (in-process)
            → REST controller
                → Use case
                    → Domain aggregate
                        → JPA (real PostgreSQL via Testcontainers)
                        → Kafka publisher (real Kafka via Testcontainers)
```

No mocks. If a scenario says "ShipmentCreated event is published", the step definition consumes the real Kafka topic and asserts the message is there.

---

## Infrastructure Setup

All acceptance tests extend or use `AcceptanceTestBase` which starts:
- PostgreSQL via `@Testcontainers` + `@Container`
- Kafka (KRaft) via `@Testcontainers` + `@Container`
- Spring Boot application context (`@SpringBootTest(webEnvironment = RANDOM_PORT)`)

See `runners/AcceptanceTestBase.java`.

---

## Adding Step Definitions for a New Feature

1. A new `.feature` file appears in `specs/acceptance-tests/` (e.g. `assign-shipment.feature`)
2. Run the test suite — Cucumber reports all scenarios in that file as `Undefined`
3. Create `step-definitions/AssignShipmentSteps.java`
4. Implement each `@Given`, `@When`, `@Then` method
5. Re-run — scenarios should now pass

---

## File Structure

```
acceptance/
  README.md                          ← this file
  runners/
    AcceptanceTestRunner.java         ← @Suite + @CucumberOptions
    AcceptanceTestBase.java           ← Testcontainers + SpringBootTest setup
  step-definitions/
    CreateShipmentSteps.java          ← implements create-shipment.feature
    AssignShipmentSteps.java          ← implements assign-shipment.feature
    CancelShipmentSteps.java          ← implements cancel-shipment.feature
    ... (one file per .feature)
```
