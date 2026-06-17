# Topic: `shipment.assigned`

## Purpose

Published when a shipment is successfully assigned to a vehicle and driver. Triggers driver notification with pickup instructions and informs the Shipper of the confirmed ETA.

## Configuration

| Property | Value |
|----------|-------|
| Partitions | 12 |
| Replication factor | 3 (production) |
| Retention | 7 days |
| Partition key | `shipmentId` |
| Dead-letter topic | `shipment.assigned.dlq` |
| Consumer group (driver) | `driver-service.shipment.assigned` |
| Consumer group (notification) | `notification-service.shipment.assigned` |

## Producer

`shipment-service` — published when Shipment transitions to ASSIGNED status.

## Consumers

| Service | Action |
|---------|--------|
| `driver-service` | Record assignment; set driver status to DRIVING when pickup confirmed |
| `notification-service` | SMS to Driver with pickup address and shipmentId; Email to Shipper with ETA |

## Payload Schema

```json
{
  "eventId": "string (UUID)",
  "eventVersion": "integer",
  "eventType": "ShipmentAssigned",
  "occurredAt": "string (ISO-8601 UTC)",
  "shipmentId": "string (UUID)",
  "vehicleId": "string (UUID)",
  "driverId": "string (UUID)",
  "routeId": "string (UUID)",
  "etaAt": "string (ISO-8601 UTC)",
  "pickupAddress": {
    "street": "string",
    "city": "string",
    "country": "string",
    "lat": "number",
    "lon": "number"
  },
  "assignedAt": "string (ISO-8601 UTC)",
  "isManualOverride": "boolean"
}
```

## Example Payload

```json
{
  "eventId": "a1b2c3d4-0000-0000-0000-000000000002",
  "eventVersion": 1,
  "eventType": "ShipmentAssigned",
  "occurredAt": "2026-06-16T09:05:00Z",
  "shipmentId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "vehicleId": "c9e3f2b4-aaaa-bbbb-cccc-111122223333",
  "driverId": "d1e4f3c5-dddd-eeee-ffff-444455556666",
  "routeId": "e2f5a4d6-1111-2222-3333-777788889999",
  "etaAt": "2026-06-18T14:00:00Z",
  "pickupAddress": {
    "street": "Industriestrasse 10",
    "city": "Berlin",
    "country": "DE",
    "lat": 52.5200,
    "lon": 13.4050
  },
  "assignedAt": "2026-06-16T09:05:00Z",
  "isManualOverride": false
}
```
