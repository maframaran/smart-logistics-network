# Feature: F-004 — Validate Driver Eligibility

**Epic:** EP-001 Shipment Management
**Domain:** Shipment / Fleet

## Goal

Determine whether a given driver can legally operate a specific shipment before an assignment is made.

## Actors

- Assignment Use Case (internal, invoked by F-005)

## Preconditions

- Driver exists and their certifications and working hours are current
- Shipment has a finalized CargoSpec

## Workflow

1. Assignment use case requests driver eligibility check for a (driver, shipment) pair
2. System checks driver status: must be AVAILABLE
3. System checks hazmat certification: if `shipment.requiresHazmat = true` then `driver.certifications` includes HAZMAT
4. System checks working hours: `driver.dailyHoursToday + estimatedTripHours ≤ 9`
5. Returns eligibility result with reason code if ineligible

## Business Rules

- BR-003: `shipment.requiresHazmat = true AND driver.hazmatCertification = false` → HAZMAT_CERTIFICATION_REQUIRED
- BR-005: `driver.dailyHoursToday + estimatedTripHours > 9` → DRIVER_HOURS_EXCEEDED

## Edge Cases

- EC-001: Driver is DRIVING, RESTING, or SUSPENDED → DRIVER_UNAVAILABLE
- EC-002: Driver not found → DRIVER_NOT_FOUND
- EC-003: Working hours log is stale (> 24h since last update) → flag for manual review, do not auto-assign

## Acceptance Criteria

- AC-001: Returns `eligible = true` when all rules pass
- AC-002: Returns `eligible = false` with specific reason code for each rule violation
- AC-003: SUSPENDED drivers are always ineligible regardless of other checks

## Telemetry

Track:
- `driver.eligibility.checked` (with result)
- `driver.eligibility.rejected` (with reason)
