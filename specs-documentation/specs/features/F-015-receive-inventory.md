# Feature: F-015 — Receive Inventory

**Epic:** EP-005 Warehouse Management
**Domain:** Warehouse

## Goal

Allow a Warehouse Operator to record inbound inventory, enforcing warehouse capacity limits before acceptance (BR-006).

## Actors

- Warehouse Operator

## Preconditions

- Warehouse exists and its current capacity is known
- Inbound inventory has a valid SKU and quantity

## Workflow

1. Warehouse Operator submits inbound inventory record (warehouseId, SKU, quantity, batchNumber, expirationDate)
2. System checks: `warehouse.currentUnits + incomingQuantity ≤ warehouse.maxUnits` (BR-006)
3. If capacity available: create InventoryItem record, update `warehouse.currentUnits`
4. Publish `InventoryReceived` event
5. If capacity exceeded: reject with `WAREHOUSE_CAPACITY_EXCEEDED`; suggest alternative warehouses with available space

## Business Rules

- BR-006: `currentCapacity + incomingInventory ≤ maxCapacity`

## Edge Cases

- EC-001: Warehouse at full capacity → reject, publish `WarehouseCapacityExceeded`, return list of alternative warehouses
- EC-002: SKU not recognized → accept and create new SKU record (no pre-registration required)
- EC-003: Expiration date in the past → reject with `INVALID_EXPIRATION_DATE`
- EC-004: Partial receipt (only some units fit) → reject whole batch; do not partially accept

## Acceptance Criteria

- AC-001: Inventory accepted when capacity allows; `currentUnits` updated
- AC-002: `InventoryReceived` event published with SKU, quantity, warehouseId
- AC-003: Rejection returns `WAREHOUSE_CAPACITY_EXCEEDED` with available alternatives
- AC-004: `WarehouseCapacityUpdated` event published after each receipt (consumed by Routing service)

## Telemetry

Track:
- `warehouse.inventory.receive.requested` (with quantity)
- `warehouse.inventory.receive.succeeded`
- `warehouse.inventory.receive.rejected` (with reason)
