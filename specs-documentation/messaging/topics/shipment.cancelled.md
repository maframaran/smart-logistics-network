# Topic: `shipment.cancelled`

## Purpose

Published when a shipment is successfully cancelled. Notifies Shipper and Carrier, and triggers any downstream cleanup (vehicle/driver release is handled within the Shipment Service synchronously before publishing).

## Configuration

| Property | Value |
|----------|-------|
| Partitions | 6 |
| Replication factor | 3 (production) |
| Retention | 7 days |
| Partition key | `shipmentId` |
| Dead-letter topic | `shipment.cancelled.dlq` |
| Consumer group (notification) | `notification-service.shipment.cancelled` |

## Producer

`shipment-service` — published when Shipment transitions to CANCELLED.

## Consumers

| Service | Action |
|---------|--------|
| `notification-service` | Email to Shipper and Carrier confirming cancellation; include fee notice if applicable |

## Payload Schema

```json
{
  "eventId": "string (UUID)",
  "eventVersion": "integer",
  "eventType": "ShipmentCancelled",
  "occurredAt": "string (ISO-8601 UTC)",
  "shipmentId": "string (UUID)",
  "shipperId": "string (UUID)",
  "cancelledAt": "string (ISO-8601 UTC)",
  "statusAtCancellation": "DRAFT | CREATED | SCHEDULED | ASSIGNED",
  "cancellationFeeApplied": "boolean",
  "reason": "string (nullable)"
}
```

## Example Payload

```json
{
  "eventId": "a1b2c3d4-0000-0000-0000-000000000005",
  "eventVersion": 1,
  "eventType": "ShipmentCancelled",
  "occurredAt": "2026-06-16T11:00:00Z",
  "shipmentId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "shipperId": "b8d2f1a3-1234-5678-9abc-def012345678",
  "cancelledAt": "2026-06-16T11:00:00Z",
  "statusAtCancellation": "SCHEDULED",
  "cancellationFeeApplied": true,
  "reason": "Customer changed order"
}
```
