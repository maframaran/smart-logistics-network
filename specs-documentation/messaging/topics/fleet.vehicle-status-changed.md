# Topic: `fleet.vehicle-status-changed`

## Purpose

Published whenever a vehicle's operational status changes. The Shipment Service uses this to keep its vehicle availability cache in sync, ensuring automated assignment always has accurate eligibility data.

## Configuration

| Property | Value |
|----------|-------|
| Partitions | 6 |
| Replication factor | 3 (production) |
| Retention | 3 days |
| Partition key | `vehicleId` |
| Dead-letter topic | `fleet.vehicle-status-changed.dlq` |
| Consumer group (shipment) | `shipment-service.fleet.vehicle-status-changed` |

## Producer

`fleet-service` — published on every valid Vehicle status transition.

## Consumers

| Service | Action |
|---------|--------|
| `shipment-service` | Update vehicle availability in assignment candidate pool |

## Payload Schema

```json
{
  "eventId": "string (UUID)",
  "eventVersion": "integer",
  "eventType": "VehicleStatusChanged",
  "occurredAt": "string (ISO-8601 UTC)",
  "vehicleId": "string (UUID)",
  "carrierId": "string (UUID)",
  "previousStatus": "AVAILABLE | ASSIGNED | MAINTENANCE | OUT_OF_SERVICE",
  "newStatus": "AVAILABLE | ASSIGNED | MAINTENANCE | OUT_OF_SERVICE",
  "reason": "string (nullable)",
  "changedAt": "string (ISO-8601 UTC)"
}
```

## Example Payload

```json
{
  "eventId": "b2c3d4e5-0000-0000-0000-000000000001",
  "eventVersion": 1,
  "eventType": "VehicleStatusChanged",
  "occurredAt": "2026-06-16T10:00:00Z",
  "vehicleId": "c9e3f2b4-aaaa-bbbb-cccc-111122223333",
  "carrierId": "aa11bb22-cccc-dddd-eeee-ff0011223344",
  "previousStatus": "AVAILABLE",
  "newStatus": "MAINTENANCE",
  "reason": "Scheduled oil change",
  "changedAt": "2026-06-16T10:00:00Z"
}
```
