# Epic: EP-003 — Driver Management

**Phase:** 1
**Domain:** Fleet (Driver aggregate)

## Problem

Carriers have no way to register drivers or enforce certification and working-hour rules, making automated assignment unsafe.

## Success Metrics

- 100% of drivers have up-to-date certification records
- Zero assignments made to drivers exceeding legal driving hours
- Driver registration completed in under 2 minutes

## Features

- F-010 Register Driver
- F-011 Update Driver Status
- F-012 Driver Hours Compliance

## Business Rules

- BR-003 Hazmat certification requirement
- BR-005 Maximum driving hours (9h/day)

## Domain Events Produced

- `DriverRegistered`
- `DriverStatusChanged`
- `DriverHoursExceeded`
