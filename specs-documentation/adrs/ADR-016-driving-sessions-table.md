# ADR-016 — Driving Sessions as a Separate Table for BR-005 Compliance

**Status:** Accepted

---

## Context

BR-005 requires that drivers cannot exceed 9 driving hours per day. The `Driver` aggregate tracks this via a `Map<LocalDate, DrivingSession>` — one session per calendar day. Persisting this map inside the `drivers` row (e.g., as JSON) would make querying and enforcing per-day uniqueness difficult.

---

## Decision

Driving sessions are stored in a dedicated **`driver.driving_sessions`** table with a `UNIQUE(driver_id, date)` constraint:

```sql
CREATE TABLE driver.driving_sessions (
    id         UUID        NOT NULL PRIMARY KEY,
    driver_id  UUID        NOT NULL REFERENCES driver.drivers(id),
    date       DATE        NOT NULL,
    hours      DECIMAL(4,2) NOT NULL,
    UNIQUE (driver_id, date)
);
```

The `DriverJpaRepository` (outbound adapter) implements a **replace-all** persistence strategy:

1. Delete all existing session rows for the driver
2. Insert all current sessions from the aggregate's map

This avoids a delta-tracking mechanism while keeping the domain model simple.

---

## Consequences

- `UNIQUE(driver_id, date)` enforced at the database level — no two rows for the same driver and day
- Replace-all is safe because `driving_sessions` are only written when a driving event occurs (low write frequency per driver)
- Querying accumulated hours for a day is a simple `SELECT WHERE driver_id = ? AND date = ?`
- If the aggregate's session map grows large (many past days), the delete-then-reinsert is inefficient; a retention policy (delete sessions older than 30 days) is recommended before production
