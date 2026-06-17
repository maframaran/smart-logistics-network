# Feature: F-002 — Schedule Shipment

**Epic:** EP-001 Shipment Management
**Domain:** Shipment

## Goal

Allow a Shipper or the system to schedule a created shipment for a specific pickup window, transitioning it to SCHEDULED status.

## Actors

- Shipper
- Platform (automated scheduling)

## Preconditions

- Shipment exists in CREATED status
- Pickup window is within warehouse operating hours (if origin is a warehouse)

## Workflow

1. Actor requests scheduling with a pickup window (earliest/latest pickup time)
2. System validates pickup window against warehouse operating hours (if applicable)
3. System validates pickup window is at least 4 hours from now (time constraint)
4. System transitions Shipment to SCHEDULED status
5. System publishes `ShipmentScheduled` event

## Business Rules

- Pickup must occur within 4 hours of assignment (time constraint from `docs/overview.md`)
- Pickup window must fall within warehouse operating hours

## Edge Cases

- EC-001: Pickup window is less than 4 hours from now → reject with `PICKUP_WINDOW_TOO_SOON`
- EC-002: Warehouse is closed during requested pickup window → reject with `WAREHOUSE_CLOSED`
- EC-003: Shipment is already SCHEDULED → return current schedule (idempotent)
- EC-004: Shipment is not in CREATED status → reject with `INVALID_STATUS_TRANSITION`

## Acceptance Criteria

- AC-001: Shipment transitions to SCHEDULED with the confirmed pickup window
- AC-002: `ShipmentScheduled` event published
- AC-003: Shipper notified of confirmed pickup window

## Telemetry

Track:
- `shipment.schedule.requested`
- `shipment.schedule.succeeded`
- `shipment.schedule.failed` (with failureReason)
