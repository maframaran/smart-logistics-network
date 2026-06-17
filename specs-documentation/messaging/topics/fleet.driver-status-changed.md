# Topic: `fleet.driver-status-changed`

## Purpose

Published whenever a driver's operational status changes. The Shipment Service uses this to keep its driver availability pool in sync for automated assignment.

## Configuration

| Property | Value |
|----------|-------|
| Partitions | 6 |
| Replication factor | 3 (production) |
| Retention | 3 days |
| Partition key | `driverId` |
| Dead-letter topic | `fleet.driver-status-changed.dlq` |
| Consumer group (shipment) | `shipment-service.fleet.driver-status-changed` |

## Producer

`driver-service` — published on every valid Driver status transition.

## Consumers

| Service | Action |
|---------|--------|
| `shipment-service` | Update driver availability in assignment candidate pool |

## Payload Schema

```json
{
  "eventId": "string (UUID)",
  "eventVersion": "integer",
  "eventType": "DriverStatusChanged",
  "occurredAt": "string (ISO-8601 UTC)",
  "driverId": "string (UUID)",
  "carrierId": "string (UUID)",
  "previousStatus": "AVAILABLE | DRIVING | RESTING | SUSPENDED",
  "newStatus": "AVAILABLE | DRIVING | RESTING | SUSPENDED",
  "reason": "string (nullable)",
  "dailyHoursToday": "number — current hours logged today (relevant for RESTING transitions)",
  "changedAt": "string (ISO-8601 UTC)"
}
```

## Example Payload

```json
{
  "eventId": "c3d4e5f6-0000-0000-0000-000000000001",
  "eventVersion": 1,
  "eventType": "DriverStatusChanged",
  "occurredAt": "2026-06-17T18:00:00Z",
  "driverId": "d1e4f3c5-dddd-eeee-ffff-444455556666",
  "carrierId": "aa11bb22-cccc-dddd-eeee-ff0011223344",
  "previousStatus": "DRIVING",
  "newStatus": "RESTING",
  "reason": "Daily driving limit reached",
  "dailyHoursToday": 9.0,
  "changedAt": "2026-06-17T18:00:00Z"
}
```
