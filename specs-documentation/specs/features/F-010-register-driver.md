# Feature: F-010 — Register Driver

**Epic:** EP-003 Driver Management
**Domain:** Fleet (Driver aggregate)

## Goal

Allow a Carrier to register a driver on the platform so they can be assigned to shipments.

## Actors

- Carrier

## Preconditions

- Carrier is authenticated
- Driver has not been previously registered (unique license number)

## Workflow

1. Carrier submits driver registration (name, license type, certifications, initial working hours)
2. System validates required fields and license type
3. System checks for duplicate registration (license number)
4. System creates Driver aggregate in AVAILABLE status with empty working hours log
5. System publishes `DriverRegistered` event

## Business Rules

- License type must be one of: B, C, CE
- Certifications are a set: HAZMAT, FOOD_TRANSPORT, OVERSIZED_LOAD (extensible)
- Working hours start at 0 for each new day (reset by scheduler)

## Edge Cases

- EC-001: License number already registered → reject with `DRIVER_ALREADY_EXISTS`
- EC-002: Invalid license type → reject with `INVALID_LICENSE_TYPE`

## Acceptance Criteria

- AC-001: Driver created with status AVAILABLE and unique driverId
- AC-002: `DriverRegistered` event published
- AC-003: Driver appears in available driver pool for assignment queries

## Telemetry

Track:
- `driver.register.requested`
- `driver.register.succeeded` (with licenseType, certifications)
- `driver.register.failed` (with reason)
