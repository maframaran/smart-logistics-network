# Feature: F-008 — Update Vehicle Status

**Epic:** EP-002 Fleet Management
**Domain:** Fleet

## Goal

Allow a Carrier or the platform to update a vehicle's operational status, reflecting real-world state changes (maintenance, breakdown, decommissioning).

## Actors

- Carrier
- Platform (automated, e.g. after delivery confirmation)

## Preconditions

- Vehicle exists
- Status transition is valid

## Valid Status Transitions

```
AVAILABLE   → ASSIGNED (by platform on assignment)
ASSIGNED    → AVAILABLE (on delivery or cancellation)
AVAILABLE   → MAINTENANCE (by Carrier)
MAINTENANCE → AVAILABLE (by Carrier, maintenance complete)
ANY         → OUT_OF_SERVICE (by Carrier or Admin)
OUT_OF_SERVICE → AVAILABLE (by Admin only)
```

## Workflow

1. Actor requests status change with reason
2. System validates the transition is permitted
3. System updates Vehicle status
4. Publishes `VehicleStatusChanged` event

## Edge Cases

- EC-001: Carrier attempts to set ASSIGNED vehicle to MAINTENANCE → reject with `INVALID_STATUS_TRANSITION` (must complete current shipment first)
- EC-002: Carrier attempts to set OUT_OF_SERVICE vehicle back to AVAILABLE → reject (admin only)

## Acceptance Criteria

- AC-001: Valid transitions update status and publish `VehicleStatusChanged`
- AC-002: Invalid transitions return `INVALID_STATUS_TRANSITION` with allowed transitions listed
- AC-003: Shipment service receives `VehicleStatusChanged` event and updates assignment availability

## Telemetry

Track:
- `vehicle.status.updated` (with previousStatus, newStatus)
- `vehicle.status.transition_rejected` (with reason)
