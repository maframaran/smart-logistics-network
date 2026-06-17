# ADR-009 — ProblemDetail (RFC 9457) for Error Responses

**Status:** Accepted

---

## Context

REST APIs need a consistent error response format. Ad-hoc error bodies (custom JSON structures per service) make client error handling harder and inconsistent across services.

---

## Decision

All services use **Spring 6's built-in `ProblemDetail`** (RFC 9457 / former RFC 7807) as the error response format. Each service has a `GlobalExceptionHandler` annotated with `@RestControllerAdvice` that maps domain exceptions to `ProblemDetail` responses.

Typical mappings:

| Exception | HTTP Status | `type` hint |
|-----------|-------------|-------------|
| `EntityNotFoundException` (or equivalent) | 404 | `/problems/not-found` |
| `IllegalArgumentException` | 400 | `/problems/invalid-request` |
| `IllegalStateException` (business rule violation) | 422 | `/problems/business-rule-violation` |
| Unhandled `Exception` | 500 | `/problems/internal-error` |

The `ProblemDetail` body includes `title`, `detail`, `status`, and `instance` (the request URI).

---

## Consequences

- Clients get a uniform error contract across all 7 services
- No custom error DTO classes needed — `ProblemDetail` is provided by `spring-web`
- RFC 9457 is an IETF standard; widely understood by API consumers and tooling
- `detail` messages must not expose internal stack traces or sensitive data in production (environment-gated)
