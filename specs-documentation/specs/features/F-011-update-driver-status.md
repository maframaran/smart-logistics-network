# Feature: F-011 — Update Driver Status

**Epic:** EP-003 Driver Management
**Domain:** Fleet (Driver aggregate)

## Goal

Allow a Carrier, Driver, or platform to update a driver's operational status.

## Actors

- Carrier
- Driver (self-update via mobile)
- Platform (automated, e.g. on shipment assignment)

## Valid Status Transitions

```
AVAILABLE → DRIVING (by platform on assignment)
DRIVING   → RESTING (by Driver, mandatory rest period)
RESTING   → AVAILABLE (by Driver, rest complete)
DRIVING   → AVAILABLE (by platform on delivery completion)
ANY       → SUSPENDED (by Carrier or Admin)
SUSPENDED → AVAILABLE (by Admin only, after review)
```

## Workflow

1. Actor requests status change with reason
2. System validates the transition
3. System updates Driver status
4. Publishes `DriverStatusChanged` event

## Edge Cases

- EC-001: Carrier attempts to unsuspend a driver → reject (admin only)
- EC-002: Driver attempts to set status to DRIVING directly (bypassing platform assignment) → reject

## Acceptance Criteria

- AC-001: Valid transitions update status and publish `DriverStatusChanged`
- AC-002: Invalid transitions return `INVALID_STATUS_TRANSITION`
- AC-003: Shipment service receives `DriverStatusChanged` event and updates assignment availability

## Telemetry

Track:
- `driver.status.updated` (with previousStatus, newStatus)
- `driver.status.transition_rejected` (with reason)
