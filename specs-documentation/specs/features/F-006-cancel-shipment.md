# Feature: F-006 — Cancel Shipment

**Epic:** EP-001 Shipment Management
**Domain:** Shipment

## Goal

Allow a Shipper or Platform Administrator to cancel a shipment, applying the correct cancellation rules based on current shipment status (BR-007).

## Actors

- Shipper
- Platform Administrator

## Preconditions

- Shipment exists
- Cancellation rules for current status permit the action (or admin override)

## Workflow

1. Actor requests cancellation with optional reason
2. System checks current shipment status against BR-007 cancellation matrix
3. Apply status-specific logic:
   - **DRAFT / CREATED**: Cancel immediately, no fee
   - **SCHEDULED**: Cancel with cancellation fee; generate fee invoice line item
   - **ASSIGNED**: Require manager (Platform Admin) approval; release vehicle and driver on approval
   - **IN_TRANSIT / DELIVERED**: Reject — cancellation forbidden
4. On approval/immediate cancellation: transition to CANCELLED, release vehicle and driver assignments
5. Publish `ShipmentCancelled` event

## Business Rules

- BR-007 Cancellation matrix:

| Status | Rule |
|--------|------|
| DRAFT | Allowed, no fee |
| CREATED | Allowed, no fee |
| SCHEDULED | Allowed with cancellation fee |
| ASSIGNED | Requires Platform Admin approval |
| PICKED_UP / IN_TRANSIT | Forbidden |
| DELIVERED | Forbidden |

## Edge Cases

- EC-001: Shipper attempts cancellation of IN_TRANSIT shipment → reject with `CANCELLATION_FORBIDDEN`
- EC-002: Shipper requests cancellation of ASSIGNED shipment → create pending approval request; notify admin
- EC-003: Admin cancels ASSIGNED shipment → release vehicle (→ AVAILABLE) and driver (→ AVAILABLE)
- EC-004: Cancellation fee invoice generation fails → cancel shipment but flag billing for manual follow-up

## Acceptance Criteria

- AC-001: DRAFT/CREATED cancellation transitions to CANCELLED immediately
- AC-002: SCHEDULED cancellation generates a cancellation fee invoice line item
- AC-003: ASSIGNED cancellation creates a pending approval request visible to Platform Admin
- AC-004: IN_TRANSIT/DELIVERED cancellation returns `CANCELLATION_FORBIDDEN`
- AC-005: `ShipmentCancelled` event published on successful cancellation
- AC-006: Vehicle and driver released to AVAILABLE status on cancellation

## Telemetry

Track:
- `shipment.cancel.requested` (with status at time of request)
- `shipment.cancel.succeeded` (with statusAtCancellation)
- `shipment.cancel.rejected` (with reason)
- `shipment.cancel.pending_approval`
