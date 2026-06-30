# ADR-029 — OpenAPI 3 Documentation via springdoc

**Status:** Accepted

---

## Context

None of the 8 REST controllers (`ShipmentController`, `InvoiceController`, `DriverController`, `VehicleController`, `RouteController`, `WarehouseController`, `NotificationController`, `RagController` — ~40 endpoints total) had any machine-readable API documentation. Phase 3 of the roadmap calls for OpenAPI as part of "Microservices" maturity. This is the first stage of the Phase 3+4+5 implementation plan — independent of every other stage, so it's done first.

## Decision

Add `springdoc-openapi-starter-webmvc-ui` (version `2.6.0`, the springdoc 2.x line targeting Spring Boot 3.3.x) to all 8 service POMs, version-managed centrally via the root `pom.xml`'s `dependencyManagement` (consistent with how `lombok` is already managed). Annotate every controller class with `@Tag` (one per service, named after the resource) and every endpoint method with `@Operation` (`summary` + `description` where the behavior isn't obvious from the method signature alone — e.g. noting BR-005/BR-006 validation, which domain event is raised, status-transition restrictions). `@ApiResponse(responseCode = "201", ...)` is added only on `POST` endpoints that return `201 Created`, since that's the one status code Spring MVC's default OpenAPI inference doesn't already get right from the method's return type.

Each service gets a new `infrastructure/config/OpenApiConfig.java` with an `@OpenAPIDefinition` bean (title/version/description) so `/v3/api-docs` and `/swagger-ui.html` show a proper service identity instead of a generic default.

**Annotation-only, not contract-first.** The OpenAPI spec is generated from the existing Spring MVC controllers and their request/response records (already named consistently — `<Action><Entity>Request`/`<Entity>Response` per `code-creation-guide.md`), not hand-written YAML/JSON consumed to generate code. This matches how the rest of this codebase works (code is the source of truth; specs-documentation describes intent, doesn't drive codegen).

### JPMS integration

7 of the 8 services use `module-info.java` ([ADR-004](ADR-004-jpms.md)); `rag-service` does not. The `@Tag`/`@Operation`/`@ApiResponse`/`@OpenAPIDefinition` annotations live in `io.swagger.v3.oas.annotations.*` (from `swagger-annotations-jakarta`, a transitive dependency of springdoc), whose JAR declares `Automatic-Module-Name: io.swagger.v3.oas.annotations` — confirmed by inspecting the resolved JAR's manifest, per this project's documented JPMS convention (`CLAUDE.md`'s "discover the correct name" instructions). Added as a plain `requires` (not `requires static`) in all 7 `module-info.java` files, since these annotations are `RUNTIME`-retention (springdoc reads them via reflection at startup to build the spec), unlike Lombok's `SOURCE`-retention annotations which only need `requires static` ([ADR-028](ADR-028-lombok-builder-getter.md)).

springdoc's own classes (`org.springdoc.*`) are never imported directly in application code — springdoc auto-configures via Spring Boot's component scanning of `@RestController` beans, so no additional `requires` lines are needed for the springdoc JARs themselves.

## Alternatives Considered

- **Contract-first (hand-written OpenAPI YAML, generate controller stubs)**: rejected — inverts this codebase's existing convention of code-as-source-of-truth, and would require regenerating stubs every time a request/response record changes, fighting the existing hexagonal `infrastructure/rest` pattern rather than annotating it.
- **`@ApiResponse` on every endpoint** (not just `201`s): rejected as noise — Spring MVC's return type (`ResponseEntity<List<X>>`, `ResponseEntity<Void>` with `.noContent()`, etc.) already gives springdoc enough to infer 200/204 correctly; only the `201 Created` case (returned via `ResponseEntity.created(location)`) benefits from an explicit annotation.

## Consequences

- New REST endpoints must carry `@Operation` going forward (added to `code-creation-guide.md`'s creation-order table).
- `/swagger-ui.html` is now reachable on every service's own port — useful for manual smoke-testing per the Phase 3+5 plan's per-stage verification steps, and as living documentation as Phase 4/5 add new endpoints (predictive maintenance, marketplace, tenancy).
- No schema validation or contract testing is introduced by this change — springdoc only documents what the controllers already do; it doesn't constrain them.

## Related

- [ADR-004 — JPMS](ADR-004-jpms.md)
- [ADR-028 — Lombok `@Builder`/`@Getter`](ADR-028-lombok-builder-getter.md) (same `requires` vs `requires static` distinction, opposite case)
- Phase 3+4+5 implementation plan (see `README.md` Roadmap section) — this is Stage 1
