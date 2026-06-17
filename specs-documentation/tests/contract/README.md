# Contract Tests

REST Assured tests that validate HTTP responses match the JSON contracts defined in `services/<service>/service.md`.

---

## Purpose

Contract tests sit between unit tests and integration tests. They verify:
1. The correct HTTP status codes are returned
2. All required JSON fields are present in responses
3. Field types match what the service descriptor specifies
4. Error codes match the values documented in the service spec

They do **not** test business logic — that belongs in unit and integration tests.

---

## When to Run

- On every pull request, against the service started with Testcontainers
- Before merging any change to a REST endpoint

---

## Source of Truth

Every contract test assertion is traceable to a field or status code in `services/<service>/service.md`. If the spec changes, the contract test must be updated first — not the other way around.

---

## Template

See `templates/ShipmentApiContractTest.java`.

---

## Naming Convention

| Scope | Suffix | Example |
|-------|--------|---------|
| Contract test | `ContractTest` | `ShipmentApiContractTest` |
