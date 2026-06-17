# Feature: F-007 — Register Vehicle

**Epic:** EP-002 Fleet Management
**Domain:** Fleet

## Goal

Allow a Carrier to register a vehicle on the platform so it becomes available for shipment assignment.

## Actors

- Carrier

## Preconditions

- Carrier is authenticated
- Vehicle has not been previously registered (unique vehicleId or plate number)

## Workflow

1. Carrier submits vehicle registration (type, capacity weight, capacity volume, fuel type, refrigerated flag, hazmat certified flag, current location)
2. System validates all required fields
3. System checks for duplicate registration (plate number)
4. System creates Vehicle aggregate in AVAILABLE status
5. System publishes `VehicleRegistered` event

## Business Rules

- Capacity weight and volume must be positive
- `refrigerated = true` is required for vehicles of type REFRIGERATED_TRUCK
- Vehicle type must be one of: TRUCK, VAN, REFRIGERATED_TRUCK, HAZMAT_TRUCK

## Edge Cases

- EC-001: Plate number already registered → reject with `VEHICLE_ALREADY_EXISTS`
- EC-002: Capacity values are zero or negative → reject with `INVALID_CAPACITY`
- EC-003: REFRIGERATED_TRUCK registered with `refrigerated = false` → reject with `INCONSISTENT_VEHICLE_TYPE`

## Acceptance Criteria

- AC-001: Vehicle created with status AVAILABLE and unique vehicleId
- AC-002: `VehicleRegistered` event published
- AC-003: Vehicle appears in available vehicle pool for assignment queries

## Telemetry

Track:
- `vehicle.register.requested`
- `vehicle.register.succeeded` (with vehicleType)
- `vehicle.register.failed` (with reason)
