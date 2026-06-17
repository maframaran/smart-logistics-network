# Topic: `shipment.created`

## Purpose

Published when a Shipper successfully creates a new shipment. Triggers route calculation, initial pricing, and shipper confirmation notification.

## Configuration

| Property | Value |
|----------|-------|
| Partitions | 12 |
| Replication factor | 3 (production) |
| Retention | 7 days |
| Partition key | `shipmentId` |
| Dead-letter topic | `shipment.created.dlq` |
| Consumer group (routing) | `routing-service.shipment.created` |
| Consumer group (billing) | `billing-service.shipment.created` |
| Consumer group (notification) | `notification-service.shipment.created` |

## Producer

`shipment-service` — published immediately after Shipment aggregate transitions to CREATED status.

## Consumers

| Service | Action |
|---------|--------|
| `routing-service` | Calculate route and publish `routing.route-calculated` |
| `billing-service` | Cache shipment SLA and cargo data for future invoice computation |
| `notification-service` | Send shipment confirmation email to Shipper |

## Payload Schema

```json
{
  "eventId": "string (UUID) — unique event identifier for idempotency",
  "eventVersion": "integer — schema version (current: 1)",
  "eventType": "ShipmentCreated",
  "occurredAt": "string (ISO-8601 UTC)",
  "shipmentId": "string (UUID)",
  "shipperId": "string (UUID)",
  "origin": {
    "street": "string",
    "city": "string",
    "country": "string (ISO 3166-1 alpha-2)",
    "lat": "number",
    "lon": "number"
  },
  "destination": {
    "street": "string",
    "city": "string",
    "country": "string (ISO 3166-1 alpha-2)",
    "lat": "number",
    "lon": "number"
  },
  "cargo": {
    "weightKg": "number",
    "volumeM3": "number",
    "requiresHazmat": "boolean",
    "requiresColdChain": "boolean"
  },
  "slaType": "STANDARD | PRIORITY | EXPRESS",
  "requiredDeliveryDate": "string (ISO-8601 UTC)"
}
```

## Example Payload

```json
{
  "eventId": "a1b2c3d4-0000-0000-0000-000000000001",
  "eventVersion": 1,
  "eventType": "ShipmentCreated",
  "occurredAt": "2026-06-16T09:00:00Z",
  "shipmentId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "shipperId": "b8d2f1a3-1234-5678-9abc-def012345678",
  "origin": {
    "street": "Industriestrasse 10",
    "city": "Berlin",
    "country": "DE",
    "lat": 52.5200,
    "lon": 13.4050
  },
  "destination": {
    "street": "Bahnhofstrasse 5",
    "city": "Munich",
    "country": "DE",
    "lat": 48.1351,
    "lon": 11.5820
  },
  "cargo": {
    "weightKg": 800,
    "volumeM3": 2.5,
    "requiresHazmat": false,
    "requiresColdChain": false
  },
  "slaType": "STANDARD",
  "requiredDeliveryDate": "2026-06-19T17:00:00Z"
}
```
