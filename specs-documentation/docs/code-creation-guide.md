# Code Creation Guide — Smart Logistics Network

How to translate a feature spec into Java code using Hexagonal Architecture, DDD, and JPMS.

---

## Section A: Reading a Spec

Every feature file in `specs/features/` maps directly to Java artifacts. Use this table to know what to create from each section of the spec:

| Spec section | Java artifact |
|--------------|--------------|
| **Actors** | If the actor is a human (Shipper, Carrier) → REST controller. If the actor is "Platform" consuming a Kafka event → Kafka consumer (inbound adapter). |
| **Workflow steps** | The body of the use case implementation method, step by step. |
| **Business Rules** | Methods on the aggregate (`Shipment.validateCargo()`, `Driver.isEligibleFor()`) that throw domain exceptions when violated. Never in the use case or controller. |
| **Domain Events produced** | Java records in `domain/events/`. The aggregate instantiates them; the use case passes them to `EventPublisher` outbound port. |
| **Kafka consumed** | Inbound Kafka adapter in `infrastructure/messaging/`. Deserialises the event and calls the appropriate use case port. |
| **Acceptance Criteria** | The `Then` clauses of Cucumber step definitions in `tests/acceptance/step-definitions/`. |
| **Telemetry** | `Micrometer` counter/timer calls in the use case implementation. |
| **Edge Cases** | Domain exceptions (`CargoSpecInvalidException`, `WarehouseCapacityExceededException`) raised by the aggregate; mapped to HTTP error codes by a `@ControllerAdvice`. |

---

## Section B: Creation Order

Always build **bottom-up** — from the innermost layer outward. This ensures the domain compiles without any infrastructure dependency at every step.

```
Step  Artifact                     Layer           Depends on
────  ───────────────────────────  ──────────────  ──────────────────────────
 1    Value Objects                domain/model    nothing
 2    Domain Events (records)      domain/events   value objects
 3    Aggregate                    domain/model    value objects, domain events
 4    Outbound Ports               domain/ports/out  aggregate types
 5    Inbound Ports (UseCases)     domain/ports/in   aggregate types, commands
 6    Commands / Queries           application     value objects
 7    Use Case Implementation      application     inbound + outbound ports
 8    JPA Entity + Adapter         infrastructure  aggregate, outbound port
 9    Kafka Publisher Adapter      infrastructure  domain event, outbound port
10    REST Controller              infrastructure  inbound port, DTOs
11    Kafka Consumer Adapter       infrastructure  inbound port
12    module-info.java             root            all of the above
13    Unit tests (aggregate)       test            aggregate, domain exceptions
14    Unit tests (use case)        test            use case, mock ports
15    Integration tests            test            full stack + Testcontainers
16    Cucumber step definitions    test            feature file, SpringBootTest
```

After each step, the domain module should compile cleanly with `mvn compile -pl <domain-module>`.

---

## Section C: Package Convention

Every service follows this exact package structure (see [ADR-005](../adrs/ADR-005-hexagonal.md)):

```
com.logistics.<domain>/
│
├── domain/
│   ├── model/               ← Aggregate, Value Objects, Enums
│   │   ├── Shipment.java
│   │   ├── ShipmentId.java
│   │   ├── CargoSpec.java
│   │   └── ShipmentStatus.java
│   │
│   ├── events/              ← Domain Events (Java records, immutable)
│   │   └── ShipmentCreated.java
│   │
│   ├── ports/
│   │   ├── in/              ← Inbound ports (UseCase interfaces)
│   │   │   └── CreateShipmentUseCase.java
│   │   └── out/             ← Outbound ports (Repository, Publisher interfaces)
│   │       ├── ShipmentRepository.java
│   │       └── ShipmentEventPublisher.java
│   │
│   └── exception/           ← Domain exceptions (extend RuntimeException)
│       └── CargoSpecInvalidException.java
│
├── application/
│   └── usecases/            ← Use case implementations
│       └── CreateShipmentService.java
│
└── infrastructure/
    ├── rest/                ← Spring REST controllers + DTOs
    │   ├── ShipmentController.java
    │   ├── CreateShipmentRequest.java
    │   └── ShipmentResponse.java
    │
    ├── persistence/         ← JPA entities + repository adapters
    │   ├── ShipmentJpaEntity.java
    │   └── ShipmentJpaRepository.java
    │
    └── messaging/           ← Kafka adapters
        ├── ShipmentKafkaPublisher.java     ← outbound (implements port)
        └── RouteCalculatedConsumer.java    ← inbound (calls use case port)
```

**Rule:** Nothing in `domain/` or `application/` may `import` from `infrastructure/`, Spring, JPA, or Kafka. This is enforced at compile time by JPMS (see [ADR-004](../adrs/ADR-004-jpms.md)).

---

## Section D: Naming Conventions

| Artifact | Pattern | Example |
|----------|---------|---------|
| Aggregate | `<Entity>` | `Shipment`, `Vehicle`, `Driver` |
| Value Object | descriptive noun | `CargoSpec`, `ShipmentId`, `Coordinates` |
| Domain Event | `<Entity><PastTense>` | `ShipmentCreated`, `VehicleStatusChanged` |
| Domain Exception | `<Condition>Exception` | `CargoSpecInvalidException`, `CapacityExceededException` |
| Command (input to use case) | `<Action><Entity>Command` | `CreateShipmentCommand`, `AssignShipmentCommand` |
| Inbound port | `<Action><Entity>UseCase` | `CreateShipmentUseCase`, `AssignShipmentUseCase` |
| Outbound port — repo | `<Entity>Repository` | `ShipmentRepository` |
| Outbound port — events | `<Entity>EventPublisher` | `ShipmentEventPublisher` |
| Use case impl | `<Action><Entity>Service` | `CreateShipmentService` |
| REST controller | `<Entity>Controller` | `ShipmentController` |
| REST request DTO | `<Action><Entity>Request` | `CreateShipmentRequest` |
| REST response DTO | `<Entity>Response` | `ShipmentResponse` |
| Kafka inbound adapter | `<EventType>Consumer` | `RouteCalculatedConsumer` |
| Kafka outbound adapter | `<Entity>KafkaPublisher` | `ShipmentKafkaPublisher` |
| JPA entity | `<Entity>JpaEntity` | `ShipmentJpaEntity` |
| JPA adapter | `<Entity>JpaRepository` | `ShipmentJpaRepository` (implements `ShipmentRepository`) |

---

## Section E: Spec → Code Walkthrough (F-001 Create Shipment)

### 1. Read the spec
Open [`specs/features/F-001-create-shipment.md`](../specs/features/F-001-create-shipment.md).

- **Actors:** Shipper → REST controller (POST `/api/v1/shipments`)
- **Workflow step 5:** "System creates Shipment aggregate in CREATED status" → `Shipment.create(...)` factory method
- **Business Rule BR-001/002:** weight and volume must be positive → validated inside `CargoSpec` value object constructor
- **Domain Event:** `ShipmentCreated` → published via `ShipmentEventPublisher`
- **Kafka:** `shipment.created` topic → schema from [`messaging/topics/shipment.created.md`](../messaging/topics/shipment.created.md)
- **Acceptance Criteria AC-002:** "ShipmentCreated event published" → `Then` step in `CreateShipmentSteps.java`

### 2. Create in bottom-up order

```
ShipmentId.java          (record, wraps UUID)
CargoSpec.java           (record, validates weight > 0 && volume > 0)
Address.java             (record, includes Coordinates)
SlaType.java             (enum)
ShipmentStatus.java      (enum, starts at CREATED)
ShipmentCreated.java     (record, fields match messaging/topics/shipment.created.md)
Shipment.java            (aggregate, create() factory, raises ShipmentCreated)
ShipmentRepository.java  (interface: save, findById)
ShipmentEventPublisher.java (interface: publish(ShipmentCreated))
CreateShipmentUseCase.java  (interface: ShipmentId create(CreateShipmentCommand))
CreateShipmentCommand.java  (record: origin, destination, cargo, slaType, deliveryDate)
CreateShipmentService.java  (implements CreateShipmentUseCase)
ShipmentJpaEntity.java   (maps Shipment ↔ DB row)
ShipmentJpaRepository.java (implements ShipmentRepository)
ShipmentKafkaPublisher.java (implements ShipmentEventPublisher)
ShipmentController.java  (maps HTTP POST → CreateShipmentUseCase)
module-info.java         (exports domain packages; does NOT require spring.*)
```

### 3. Verify spec coverage
After implementation, re-read each **Acceptance Criterion** and confirm it is asserted in a test:
- AC-001 → `CreateShipmentIT`: response contains `shipmentId` and `status = CREATED`
- AC-002 → `CreateShipmentIT`: Kafka consumer asserts `ShipmentCreated` message on `shipment.created`
- AC-003 → `CreateShipmentIT`: response time < 500ms
- AC-004 → `CreateShipmentSteps`: `Then the request is rejected with error code INVALID_CARGO_SPEC`

---

## Section F: Business Rule Checklist

Before marking a feature as complete, verify every Business Rule in the feature spec is:

- [ ] Enforced by the **aggregate or value object** (not the use case or controller)
- [ ] Covered by a **unit test** on the aggregate method
- [ ] Covered by at least one **Cucumber scenario** in the `.feature` file
- [ ] Returns the correct **error code** to the HTTP caller (via `@ControllerAdvice`)

---

## Section G: Adding a New Feature

1. Read the feature spec in `specs/features/F-XXX-<name>.md`
2. Read the related epic in `specs/epics/`
3. Read the relevant domain model section in `architecture/domains.md`
4. If the feature produces a new Kafka topic, read `messaging/topics/<topic>.md` for the exact payload schema
5. If the feature exposes a new REST endpoint, read `services/<service>/service.md` for the exact request/response contract
6. Follow Section B creation order
7. Open the corresponding `.feature` file in `specs/acceptance-tests/` and add step definitions in `tests/acceptance/step-definitions/`

See `docs/templates/` for a complete working example of every artifact.
