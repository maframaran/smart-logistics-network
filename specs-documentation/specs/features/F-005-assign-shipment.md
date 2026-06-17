# Feature: F-005 — Assign Shipment

**Epic:** EP-001 Shipment Management
**Domain:** Shipment

## Goal

Automatically assign an eligible vehicle and driver to a scheduled shipment, verify route feasibility against the SLA, and transition the shipment to ASSIGNED status.

## Actors

- Platform (automated)
- Platform Administrator (manual override)

## Preconditions

- Shipment is in SCHEDULED status
- At least one vehicle is AVAILABLE
- At least one driver is AVAILABLE
- Route service is reachable (async via Kafka)

## Workflow

1. System retrieves available vehicles matching cargo spec (F-003 eligibility)
2. System retrieves available drivers (F-004 eligibility)
3. For each candidate (vehicle, driver) pair:
   a. Validate vehicle eligibility (F-003)
   b. Validate driver eligibility (F-004)
   c. Request route calculation (publishes request, awaits `RouteCalculated` event)
   d. Validate ETA ≤ promisedDeliveryDate (BR-004)
4. Select best pair (minimize cost: distance + fuel + tolls)
5. Transition Shipment to ASSIGNED with vehicleId, driverId, routeId
6. Publish `ShipmentAssigned` event
7. If no eligible pair found → publish `ShipmentAssignmentFailed` and alert operations

**Manual Override:**
- Platform Administrator can force-assign a specific vehicle and driver
- Manual override bypasses optimization but still enforces BR-001, BR-002, BR-003, BR-008
- Manual override of BR-005 requires explicit override flag and audit log entry

## Business Rules

- BR-001, BR-002: Weight/volume via F-003
- BR-003: Hazmat certification via F-004
- BR-004: ETA must satisfy promisedDeliveryDate
- BR-005: Driver hours via F-004
- BR-008: Cold chain via F-003

## Edge Cases

- EC-001: No available vehicles → `ASSIGNMENT_FAILED: NO_VEHICLES_AVAILABLE`
- EC-002: No available drivers → `ASSIGNMENT_FAILED: NO_DRIVERS_AVAILABLE`
- EC-003: All feasible routes exceed SLA → `ASSIGNMENT_FAILED: SLA_INFEASIBLE`
- EC-004: Route calculation times out (> 10s) → retry once, then fail with `ROUTING_TIMEOUT`
- EC-005: Shipment already ASSIGNED → return current assignment (idempotent)

## Acceptance Criteria

- AC-001: Shipment transitions to ASSIGNED with valid vehicleId, driverId, routeId, etaAt
- AC-002: `ShipmentAssigned` event published to `shipment.assigned` topic
- AC-003: Assignment completed within 2 minutes (success metric from EP-001)
- AC-004: Manual override creates audit log entry
- AC-005: Assignment failure publishes `ShipmentAssignmentFailed` and notifies operations

## Telemetry

Track:
- `shipment.assignment.started`
- `shipment.assignment.succeeded` (with vehicleType, driverId, etaHours)
- `shipment.assignment.failed` (with reason)
- `shipment.assignment.manual_override` (with adminId)
