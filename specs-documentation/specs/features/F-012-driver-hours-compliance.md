# Feature: F-012 — Driver Hours Compliance

**Epic:** EP-003 Driver Management
**Domain:** Fleet (Driver aggregate)

## Goal

Enforce legal driving hour limits by tracking each driver's daily hours and preventing assignment or continued driving when limits are reached (BR-005).

## Actors

- Platform (automated enforcement)
- Driver (logs actual hours via mobile)

## Preconditions

- Driver is registered with an active working hours log
- Daily hours log resets at midnight (driver's local timezone)

## Workflow

### Hour Logging
1. Driver reports driving session end (or platform infers from shipment status)
2. System records `sessionHours` in driver's `WorkingHoursLog`
3. System checks `dailyHoursToday + sessionHours`
4. If limit reached: transition Driver to RESTING, publish `DriverHoursExceeded`

### Pre-Assignment Check
1. Assignment use case requests hours compliance check for (driver, estimatedTripHours)
2. System checks: `driver.dailyHoursToday + estimatedTripHours ≤ 9`
3. Returns eligible or DRIVER_HOURS_EXCEEDED

### Daily Reset
- Scheduled job resets `dailyHoursToday = 0` at midnight (driver's local timezone)
- Weekly hours tracking maintained separately for regulatory reporting

## Business Rules

- BR-005: Maximum 9 driving hours per day

## Edge Cases

- EC-001: Hours log not updated in > 12 hours → flag for manual review, block automated assignment
- EC-002: Driver attempts to start driving with 0 hours remaining → reject, suggest rest period
- EC-003: Clock rollover at midnight mid-journey → log hours split across two days

## Acceptance Criteria

- AC-001: Assignment rejected when `dailyHoursToday + estimatedTripHours > 9`
- AC-002: `DriverHoursExceeded` event published when limit reached
- AC-003: Driver status transitions to RESTING automatically on limit reached
- AC-004: Daily reset restores eligibility at midnight

## Telemetry

Track:
- `driver.hours.logged` (with sessionHours, dailyTotalHours)
- `driver.hours.limit_reached` (with driverId)
- `driver.hours.daily_reset`
