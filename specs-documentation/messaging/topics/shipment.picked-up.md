# Topic: `shipment.picked-up`

## Purpose

Published when the Driver confirms pickup of the cargo, transitioning the shipment to IN_TRANSIT. Notifies the Shipper that their goods are on the way.

## Configuration

| Property | Value |
|----------|-------|
| Partitions | 12 |
| Replication factor | 3 (production) |
| Retention | 7 days |
| Partition key | `shipmentId` |
| Dead-letter topic | `shipment.picked-up.dlq` |
| Consumer group (notification) | `notification-service.shipment.picked-up` |

## Producer

`shipment-service` — published when Driver confirms pickup via the Driver mobile interface.

## Consumers

| Service | Action |
|---------|--------|
| `notification-service` | Email/SMS to Shipper: "Your cargo has been picked up and is in transit" |

## Payload Schema

```json
{
  "eventId": "string (UUID)",
  "eventVersion": "integer",
  "eventType": "ShipmentPickedUp",
  "occurredAt": "string (ISO-8601 UTC)",
  "shipmentId": "string (UUID)",
  "driverId": "string (UUID)",
  "vehicleId": "string (UUID)",
  "pickedUpAt": "string (ISO-8601 UTC)",
  "currentLocation": {
    "lat": "number",
    "lon": "number"
  }
}
```

## Example Payload

```json
{
  "eventId": "a1b2c3d4-0000-0000-0000-000000000003",
  "eventVersion": 1,
  "eventType": "ShipmentPickedUp",
  "occurredAt": "2026-06-17T08:30:00Z",
  "shipmentId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "driverId": "d1e4f3c5-dddd-eeee-ffff-444455556666",
  "vehicleId": "c9e3f2b4-aaaa-bbbb-cccc-111122223333",
  "pickedUpAt": "2026-06-17T08:30:00Z",
  "currentLocation": { "lat": 52.5200, "lon": 13.4050 }
}
```
