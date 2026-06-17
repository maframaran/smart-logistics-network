# Topic: `shipment.delivered`

## Purpose

Published when delivery is confirmed by the Driver or system. Triggers invoice generation, carrier payment creation, and delivery confirmation notification to the Shipper. Long retention supports billing audit trails.

## Configuration

| Property | Value |
|----------|-------|
| Partitions | 12 |
| Replication factor | 3 (production) |
| Retention | 30 days |
| Partition key | `shipmentId` |
| Dead-letter topic | `shipment.delivered.dlq` |
| Consumer group (billing) | `billing-service.shipment.delivered` |
| Consumer group (notification) | `notification-service.shipment.delivered` |

## Producer

`shipment-service` — published when Shipment transitions to DELIVERED.

## Consumers

| Service | Action |
|---------|--------|
| `billing-service` | Generate shipper invoice + create carrier payment in PENDING |
| `notification-service` | Email to Shipper: delivery confirmed with timestamp and signature |

## Payload Schema

```json
{
  "eventId": "string (UUID)",
  "eventVersion": "integer",
  "eventType": "ShipmentDelivered",
  "occurredAt": "string (ISO-8601 UTC)",
  "shipmentId": "string (UUID)",
  "shipperId": "string (UUID)",
  "carrierId": "string (UUID)",
  "driverId": "string (UUID)",
  "deliveredAt": "string (ISO-8601 UTC)",
  "promisedDeliveryDate": "string (ISO-8601 UTC)",
  "slaType": "STANDARD | PRIORITY | EXPRESS",
  "signedBy": "string (recipient name, nullable)",
  "deliveryLocation": {
    "lat": "number",
    "lon": "number"
  }
}
```

## Example Payload

```json
{
  "eventId": "a1b2c3d4-0000-0000-0000-000000000004",
  "eventVersion": 1,
  "eventType": "ShipmentDelivered",
  "occurredAt": "2026-06-18T13:45:00Z",
  "shipmentId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "shipperId": "b8d2f1a3-1234-5678-9abc-def012345678",
  "carrierId": "aa11bb22-cccc-dddd-eeee-ff0011223344",
  "driverId": "d1e4f3c5-dddd-eeee-ffff-444455556666",
  "deliveredAt": "2026-06-18T13:45:00Z",
  "promisedDeliveryDate": "2026-06-19T17:00:00Z",
  "slaType": "STANDARD",
  "signedBy": "Klaus Müller",
  "deliveryLocation": { "lat": 48.1351, "lon": 11.5820 }
}
```
