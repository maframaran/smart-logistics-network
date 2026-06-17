# Feature: F-017 — Warehouse Capacity Management

**Epic:** EP-005 Warehouse Management
**Domain:** Warehouse

## Goal

Allow a Platform Administrator to configure warehouse capacity and operating hours, and provide operators with a real-time capacity dashboard.

## Actors

- Platform Administrator
- Warehouse Operator (read-only view)

## Preconditions

- Warehouse exists

## Workflow

### Update Capacity/Hours
1. Admin submits updated `maxUnits` and/or `operatingHours`
2. System validates: new `maxUnits` ≥ `currentUnits` (cannot set max below current stock)
3. System updates Warehouse aggregate
4. Publishes `WarehouseCapacityUpdated` event

### Capacity Query
1. Operator or system queries warehouse utilization
2. System returns: `currentUnits`, `maxUnits`, utilization percentage, last updated timestamp

## Edge Cases

- EC-001: Admin attempts to reduce `maxUnits` below `currentUnits` → reject with `CAPACITY_BELOW_CURRENT_STOCK`
- EC-002: Warehouse has no inventory → allow any positive `maxUnits`

## Acceptance Criteria

- AC-001: Capacity and hours update reflected immediately in all future inventory checks
- AC-002: `WarehouseCapacityUpdated` event published on any capacity change
- AC-003: Utilization query returns real-time values

## Telemetry

Track:
- `warehouse.capacity.updated` (with newMaxUnits, utilizationPercent)
