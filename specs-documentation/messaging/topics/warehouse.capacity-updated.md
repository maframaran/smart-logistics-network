# Topic: `warehouse.capacity-updated`

## Purpose

Published after any inventory receipt or dispatch that changes a warehouse's current unit count. Allows the Routing Service to factor warehouse availability into route feasibility checks, and triggers ops alerts when utilization thresholds are breached.

## Configuration

| Property | Value |
|----------|-------|
| Partitions | 4 |
| Replication factor | 3 (production) |
| Retention | 3 days |
| Partition key | `warehouseId` |
| Dead-letter topic | `warehouse.capacity-updated.dlq` |
| Consumer group (routing) | `routing-service.warehouse.capacity-updated` |
| Consumer group (notification) | `notification-service.warehouse.capacity-updated` |

## Producer

`warehouse-service` — published after every inventory receipt or outbound dispatch.

## Consumers

| Service | Action |
|---------|--------|
| `routing-service` | Update warehouse availability for route feasibility calculations |
| `notification-service` | Alert ops team when `utilizationPercent ≥ 90` |

## Payload Schema

```json
{
  "eventId": "string (UUID)",
  "eventVersion": "integer",
  "eventType": "WarehouseCapacityUpdated",
  "occurredAt": "string (ISO-8601 UTC)",
  "warehouseId": "string (UUID)",
  "previousUnits": "integer",
  "currentUnits": "integer",
  "maxUnits": "integer",
  "utilizationPercent": "number",
  "changeType": "INBOUND | OUTBOUND | CONFIG_UPDATE",
  "relatedShipmentId": "string (UUID, nullable — set for OUTBOUND changes)",
  "updatedAt": "string (ISO-8601 UTC)"
}
```

## Example Payload

```json
{
  "eventId": "d4e5f6a7-0000-0000-0000-000000000001",
  "eventVersion": 1,
  "eventType": "WarehouseCapacityUpdated",
  "occurredAt": "2026-06-16T14:00:00Z",
  "warehouseId": "e5f6a7b8-1111-2222-3333-444455556666",
  "previousUnits": 600,
  "currentUnits": 900,
  "maxUnits": 1000,
  "utilizationPercent": 90.0,
  "changeType": "INBOUND",
  "relatedShipmentId": null,
  "updatedAt": "2026-06-16T14:00:00Z"
}
```
