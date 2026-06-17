# Feature: F-003 — Validate Vehicle Eligibility

**Epic:** EP-001 Shipment Management
**Domain:** Shipment / Fleet

## Goal

Determine whether a given vehicle can legally and physically carry a specific shipment before an assignment is made.

## Actors

- Assignment Use Case (internal, invoked by F-005)

## Preconditions

- Vehicle exists and its current status and capacity are known
- Shipment has a finalized CargoSpec

## Workflow

1. Assignment use case requests vehicle eligibility check for a (vehicle, shipment) pair
2. System checks weight capacity: `shipment.weight ≤ vehicle.maxWeightKg`
3. System checks volume capacity: `shipment.volume ≤ vehicle.maxVolumeM3`
4. System checks cold chain: if `shipment.requiresColdChain = true` then `vehicle.refrigerated = true`
5. System checks vehicle status: must be AVAILABLE
6. Returns eligibility result with reason code if ineligible

## Business Rules

- BR-001: `shipment.weight > vehicle.maxWeightKg` → CAPACITY_WEIGHT_EXCEEDED
- BR-002: `shipment.volume > vehicle.maxVolumeM3` → CAPACITY_VOLUME_EXCEEDED
- BR-008: `shipment.requiresColdChain = true AND vehicle.refrigerated = false` → COLD_CHAIN_REQUIRED

## Edge Cases

- EC-001: Vehicle is MAINTENANCE or OUT_OF_SERVICE → VEHICLE_UNAVAILABLE
- EC-002: Vehicle is already ASSIGNED → VEHICLE_UNAVAILABLE
- EC-003: Vehicle not found → VEHICLE_NOT_FOUND

## Acceptance Criteria

- AC-001: Returns `eligible = true` when all rules pass
- AC-002: Returns `eligible = false` with specific reason code for each rule violation
- AC-003: Multiple violations return the first violated rule (fail-fast)

## Telemetry

Track:
- `vehicle.eligibility.checked` (with result and vehicleType)
- `vehicle.eligibility.rejected` (with reason)
