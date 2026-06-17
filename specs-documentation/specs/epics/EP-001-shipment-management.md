# Epic: EP-001 — Shipment Management

**Phase:** 1
**Domain:** Shipment

## Problem

Shippers have no way to create, schedule, assign, track, or cancel shipments on the platform.

## Success Metrics

- 95% of shipments automatically assigned within 2 minutes of creation
- 99% SLA compliance across all delivery tiers
- Zero shipments lost due to system state inconsistency

## Features

- F-001 Create Shipment
- F-002 Schedule Shipment
- F-003 Validate Vehicle Eligibility
- F-004 Validate Driver Eligibility
- F-005 Assign Shipment
- F-006 Cancel Shipment

## Business Rules

- BR-001 Vehicle weight capacity
- BR-002 Vehicle volume capacity
- BR-003 Driver hazmat certification
- BR-004 Delivery SLA compliance
- BR-007 Cancellation by status
- BR-008 Cold chain vehicle requirement

## Domain Events Produced

- `ShipmentCreated`
- `ShipmentScheduled`
- `ShipmentAssigned`
- `ShipmentPickedUp`
- `ShipmentInTransit`
- `ShipmentDelivered`
- `ShipmentCancelled`
- `ShipmentFailed`
