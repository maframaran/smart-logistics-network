# Feature: F-001 — Create Shipment

**Epic:** EP-001 Shipment Management
**Domain:** Shipment

## Goal

Allow a Shipper to create a new shipment request on the platform, transitioning it from DRAFT to CREATED status and triggering downstream routing and pricing.

## Actors

- Shipper

## Preconditions

- Shipper is authenticated
- Origin and destination addresses are valid and geocodable

## Workflow

1. Shipper submits shipment request (origin, destination, cargo spec, SLA type, required delivery date)
2. System validates all required fields
3. System geocodes origin and destination addresses
4. System creates Shipment aggregate in CREATED status
5. System publishes `ShipmentCreated` event to Kafka
6. System returns shipment ID and initial status to Shipper

## Business Rules

- BR-001 / BR-002: Cargo weight and volume must be positive values
- BR-004: Required delivery date must be in the future
- Cargo spec must declare `requiresHazmat` and `requiresColdChain` flags explicitly

## Edge Cases

- EC-001: Geocoding fails for address → return validation error with field detail
- EC-002: Required delivery date is in the past → reject with `INVALID_DELIVERY_DATE`
- EC-003: Weight or volume is zero or negative → reject with `INVALID_CARGO_SPEC`
- EC-004: Shipper submits duplicate request (same origin/destination/date within 1 minute) → warn but allow

## Acceptance Criteria

- AC-001: Shipment created with status CREATED and unique shipmentId
- AC-002: `ShipmentCreated` event published to `shipment.created` Kafka topic
- AC-003: Shipper receives shipmentId in response within 500ms
- AC-004: Invalid requests return structured error with field-level detail

## Telemetry

Track:
- `shipment.create.requested`
- `shipment.create.succeeded` (with slaType, cargoWeightKg)
- `shipment.create.failed` (with failureReason)
