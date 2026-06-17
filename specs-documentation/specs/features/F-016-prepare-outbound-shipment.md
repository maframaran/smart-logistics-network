# Feature: F-016 — Prepare Outbound Shipment

**Epic:** EP-005 Warehouse Management
**Domain:** Warehouse

## Goal

Allow a Warehouse Operator to pick and stage inventory for an outbound shipment, reducing warehouse current capacity and confirming readiness for pickup.

## Actors

- Warehouse Operator

## Preconditions

- Shipment is in SCHEDULED or ASSIGNED status with origin at this warehouse
- Required inventory (SKU, quantity) is available in the warehouse

## Workflow

1. Warehouse Operator receives outbound order (triggered by `ShipmentAssigned` event or manual query)
2. Operator confirms inventory pick: SKU, quantity, batchNumber
3. System validates inventory availability
4. System reduces `warehouse.currentUnits` by outbound quantity
5. System records InventoryItem as dispatched
6. System publishes `InventoryDispatched` event
7. Warehouse Operator marks shipment as "ready for pickup"

## Edge Cases

- EC-001: Required SKU not in stock → alert operations, block shipment from being picked up
- EC-002: Insufficient quantity for SKU → partial fulfillment flagged, operations notified
- EC-003: Batch has expired items → flag for quality review before dispatch

## Acceptance Criteria

- AC-001: `warehouse.currentUnits` decremented by outbound quantity
- AC-002: `InventoryDispatched` event published
- AC-003: Shipment marked ready for pickup within the warehouse's system

## Telemetry

Track:
- `warehouse.outbound.prepared` (with shipmentId, quantity)
- `warehouse.outbound.blocked` (with reason)
