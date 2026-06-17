# Feature: F-009 — Vehicle Capacity Check

**Epic:** EP-002 Fleet Management
**Domain:** Fleet

## Goal

Provide a query to retrieve vehicles that can physically accommodate a given cargo spec. Used by the assignment use case (F-005) to build the candidate vehicle pool.

## Actors

- Assignment Use Case (internal)
- Platform Administrator (ad-hoc query)

## Preconditions

- Cargo spec is fully specified (weight, volume, requiresColdChain, requiresHazmat)

## Workflow

1. Caller submits cargo spec query
2. System filters vehicles by:
   - Status = AVAILABLE
   - `maxWeightKg ≥ cargoSpec.weightKg`
   - `maxVolumeM3 ≥ cargoSpec.volumeM3`
   - `refrigerated = true` if `cargoSpec.requiresColdChain = true`
3. Returns list of eligible vehicles sorted by available capacity (ascending — best fit first)

## Business Rules

- BR-001: Weight filter
- BR-002: Volume filter
- BR-008: Cold chain filter

## Edge Cases

- EC-001: No vehicles match → return empty list (caller handles `NO_VEHICLES_AVAILABLE`)
- EC-002: All matching vehicles are ASSIGNED → return empty list

## Acceptance Criteria

- AC-001: Returns only AVAILABLE vehicles satisfying all capacity constraints
- AC-002: Results are sorted best-fit first (smallest sufficient capacity)
- AC-003: Query completes within 100ms for up to 10,000 vehicles

## Telemetry

Track:
- `vehicle.capacity_query.executed` (with cargoWeightKg, resultCount)
