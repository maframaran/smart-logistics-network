# Epic: EP-002 — Fleet Management

**Phase:** 1
**Domain:** Fleet

## Problem

Carriers have no way to register vehicles or track their availability and status on the platform, blocking automated shipment assignment.

## Success Metrics

- 100% of active vehicles have accurate real-time status
- Vehicle registration completed in under 1 minute
- Zero assignment attempts made to vehicles that are unavailable or over capacity

## Features

- F-007 Register Vehicle
- F-008 Update Vehicle Status
- F-009 Vehicle Capacity Check

## Business Rules

- BR-001 Vehicle weight capacity
- BR-002 Vehicle volume capacity
- BR-008 Cold chain (refrigerated flag)

## Domain Events Produced

- `VehicleRegistered`
- `VehicleStatusChanged`
