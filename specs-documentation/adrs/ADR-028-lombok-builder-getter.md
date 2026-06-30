# ADR-028 — Lombok `@Builder`/`@Getter` for Pure-Assignment Aggregate Constructors

**Status:** Accepted

---

## Context

A SonarQube pass flagged `S107` ("too many constructor/method parameters", threshold 7) on 11 spots across 5 domain aggregates: `Invoice` (billing-service), `Driver` (driver-service), `Notification` (notification-service), `Route` (routing-service), `Shipment` (shipment-service). Each aggregate follows the project's standard pattern ([ADR-008](ADR-008-aggregate-factories.md)): a private constructor plus `create()`/`generate()`/`register()` (validates, raises a domain event) and `reconstitute()` (no validation, used by the JPA adapter) static factories.

Before making any change, every flagged constructor was inspected for hidden logic:

- **`Invoice`, `Notification`, `Shipment`**: constructors are pure field assignment — no validation, no defensive copying. Safe to regenerate.
- **`Driver`**: constructor does `this.drivingSessions = new HashMap<>(drivingSessions);` — a defensive copy into a **mutable** map, because `Driver.recordDriving()` later calls `drivingSessions.put(...)` to mutate it in place (BR-005 hour tracking). Not safe to regenerate naively.
- **`Route`**: constructor does `this.segments = List.copyOf(segments);` — a defensive copy into an **immutable** list (segments are never mutated after construction). Safe to regenerate using Lombok's `@Singular`, which produces an immutable collection at `build()` time.

Of the 11 `S107` findings, 6 are on static factory methods (`reconstitute`, `calculate`, `generate`), not constructors — Lombok only generates constructors/getters/setters/builders, never arbitrary factory methods, so these 6 are unreachable by any Lombok annotation and remain unfixed (they reflect genuine parameter counts callers need to supply; bundling them into a parameter object was judged a larger, separate refactor not undertaken here).

## Decision

Add `org.projectlombok:lombok` (`provided` scope) to the 4 modules with at least one safely-regenerable aggregate (`billing-service`, `notification-service`, `shipment-service`, `routing-service`). Apply:

- **`@Getter`** (class-level) — replaces hand-written pass-through getters. Used on all 5 classes is not safe; see exclusions below.
- **`@Builder(access = AccessLevel.PRIVATE)`** on the existing hand-written constructor (not class-level, since the constructor body is kept explicit rather than fully Lombok-generated) — makes `ClassName.builder()` only callable from within the class itself, so external code can't bypass the validating static factories. The constructor body, parameter list, and field assignments are otherwise unchanged.
- **`@Singular`** on `Route`'s `segments` builder parameter — replicates the existing `List.copyOf(segments)` defensive-copy behavior automatically at `build()` time.

Applied to: `Invoice`, `Notification`, `Shipment` (plain `@Builder` + `@Getter`), `Route` (`@Builder` with `@Singular` segments + `@Getter`).

**Not applied to `Driver`** — its `drivingSessions` map is defensively copied into a *mutable* `HashMap` and mutated after construction; Lombok's `@Singular` only produces immutable collections, and plain `@Builder` (no `@Singular`) would drop the defensive copy entirely, reintroducing aliasing risk. `Driver` keeps its hand-written constructor and getters unchanged.

**`@Setter` is not used anywhere.** All 5 aggregates mutate state only through guarded business methods (`Invoice.markPaid()`, `Driver.updateStatus()`, `Shipment.assign()`/`cancel()`, etc.) that enforce status-transition rules and raise domain events. A Lombok-generated public setter on any field would let callers bypass those invariants entirely. `@Builder`'s per-field methods only exist on the transient, package-private-constructed `Builder` instance during object construction — once `.build()` returns the aggregate, there is no setter-like access at all, which is what makes `@Builder` compatible with this constraint where `@Setter` would not be.

**`@Data` is explicitly not used** — it bundles `@Setter` (incompatible with the above), plus `@EqualsAndHashCode`/`@ToString` based on all fields, which is wrong for entities normally compared by identity (ID), not full-field equality.

### JPMS integration

All 4 modules use `module-info.java` (per [ADR-004](ADR-004-jpms.md)). Lombok's annotations are source-retention only (stripped before bytecode generation), but javac must still resolve them while parsing the annotated source — this requires the module to declare:

```java
requires static lombok;
```

The `static` modifier marks the dependency compile-time-only (matching Lombok's `provided` Maven scope and its complete absence from the runtime classpath/module graph). No `requires lombok;` (without `static`) is used anywhere, and no service exports anything Lombok-related.

`@Value`-based property injection (used separately for [ADR-027](ADR-027-kafka-topic-config-dispatch.md)'s topic externalization) additionally required `requires spring.beans;` in the same 4 modules plus `fleet-service`, `driver-service`, `warehouse-service` (the `org.springframework.beans.factory.annotation` package, previously unused directly).

## Alternatives Considered

- **Apply `@Builder`/`@AllArgsConstructor` to all 5 classes including `Driver`.** Rejected — would silently drop the mutable-map defensive copy, reintroducing exactly the kind of mutable-state-exposure bug fixed elsewhere this session (`List.copyOf(...)` compact constructors on `rag-service`'s domain records, see prior `SpotBugs EI_EXPOSE_REP2` cleanup).
- **`@AllArgsConstructor(access = PRIVATE)` instead of `@Builder`.** Considered first, but only resolves 3 of 11 `S107` findings (excludes `Route`, since `@AllArgsConstructor` has no equivalent of `@Singular`'s defensive immutable-copy behavior for the `segments` list). `@Builder` covers one more class for the same risk profile.
- **Bundle factory-method parameters into command/parameter objects** to also address the 6 method-level `S107` findings. Rejected as out of scope for this pass — a real design change touching call sites in `application/usecases` across 5 services, with no live stack available to verify; left as a candidate for a separate, dedicated piece of work.

## Consequences

- 3 of 11 `S107` findings remain (`Driver`'s constructor + all 6 factory-method findings) — expected and documented, not a regression.
- Any new aggregate constructor that is pure field assignment (no validation, no defensive copying) should default to `@Builder(access = AccessLevel.PRIVATE)` + `@Getter` rather than hand-written boilerplate. A constructor containing validation or defensive-copy logic should stay hand-written, or use `@Singular` if the only non-trivial logic is collection immutability.
- `@Setter` and `@Data` are not approved for domain aggregates under any circumstance while mutation is guarded by business methods; this should be treated as a standing constraint for future Lombok adoption, not just a one-time decision for these 5 classes.

## Related

- [ADR-004 — JPMS](ADR-004-jpms.md)
- [ADR-005 — Hexagonal Architecture](ADR-005-hexagonal.md)
- [ADR-008 — Aggregate Factories](ADR-008-aggregate-factories.md)
- [ADR-027 — Map-Based Kafka Topic Dispatch with Externalized Topic Names](ADR-027-kafka-topic-config-dispatch.md) (same session, same `requires spring.beans` JPMS addition)
