# ADR-007 — Java Records for Value Objects and Domain Events

**Status:** Accepted

---

## Context

Value objects and domain events are immutable by definition: once created they never change. Implementing them as traditional classes requires manual `equals`, `hashCode`, `toString`, and constructor boilerplate, which increases noise and the risk of mutable state leaking in.

---

## Decision

All **value objects** and **domain events** are implemented as **Java 21 records**.

Examples:
- Value objects: `ShipmentId`, `CargoSpec`, `Address`, `Money`, `Coordinates`, `SKU`, `Capacity`
- Domain events: `ShipmentCreated`, `ShipmentAssigned`, `ShipmentCancelled`, `VehicleRegistered`, `InvoiceGenerated`, etc.

Records provide:
- Immutability by default (all fields are `final`)
- Auto-generated `equals`, `hashCode`, `toString` based on components
- Compact constructor syntax for validation

Aggregates are **not** records — they are mutable classes that change state through lifecycle methods.

---

## Consequences

- No mutable value objects or domain events — invariants hold without defensive copying
- Compact notation reduces boilerplate; each record's validation is in the canonical (compact) constructor
- Records cannot extend classes, which is acceptable since value objects and events have no shared mutable state
- JPA entities cannot be records (JPA requires no-arg constructors and mutable fields); they remain standard classes alongside the domain records
