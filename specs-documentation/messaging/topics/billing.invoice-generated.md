# Topic: `billing.invoice-generated`

## Purpose

Published when the Billing Service creates and issues a shipper invoice. The Notification Service uses this to send the invoice to the Shipper. Long retention supports audit and financial reporting.

## Configuration

| Property | Value |
|----------|-------|
| Partitions | 4 |
| Replication factor | 3 (production) |
| Retention | 90 days |
| Partition key | `invoiceId` |
| Dead-letter topic | `billing.invoice-generated.dlq` |
| Consumer group (notification) | `notification-service.billing.invoice-generated` |

## Producer

`billing-service` — published when an Invoice transitions to ISSUED status.

## Consumers

| Service | Action |
|---------|--------|
| `notification-service` | Email invoice PDF link to Shipper |

## Payload Schema

```json
{
  "eventId": "string (UUID)",
  "eventVersion": "integer",
  "eventType": "InvoiceGenerated",
  "occurredAt": "string (ISO-8601 UTC)",
  "invoiceId": "string (UUID)",
  "shipmentId": "string (UUID)",
  "shipperId": "string (UUID)",
  "lineItems": [
    {
      "description": "string",
      "quantity": "number",
      "unitPriceEur": "number",
      "totalEur": "number"
    }
  ],
  "totalAmountEur": "number",
  "hasPenalty": "boolean",
  "penaltyAmountEur": "number (0 if no penalty)",
  "slaType": "STANDARD | PRIORITY | EXPRESS",
  "issuedAt": "string (ISO-8601 UTC)"
}
```

## Example Payload

```json
{
  "eventId": "f6a7b8c9-0000-0000-0000-000000000001",
  "eventVersion": 1,
  "eventType": "InvoiceGenerated",
  "occurredAt": "2026-06-18T13:50:00Z",
  "invoiceId": "a7b8c9d0-aaaa-bbbb-cccc-111122223333",
  "shipmentId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "shipperId": "b8d2f1a3-1234-5678-9abc-def012345678",
  "lineItems": [
    {
      "description": "Base transportation cost — Berlin to Munich, 584km",
      "quantity": 1,
      "unitPriceEur": 320.00,
      "totalEur": 320.00
    },
    {
      "description": "Toll costs",
      "quantity": 1,
      "unitPriceEur": 32.50,
      "totalEur": 32.50
    }
  ],
  "totalAmountEur": 352.50,
  "hasPenalty": false,
  "penaltyAmountEur": 0.00,
  "slaType": "STANDARD",
  "issuedAt": "2026-06-18T13:50:00Z"
}
```
