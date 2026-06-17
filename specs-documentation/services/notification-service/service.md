# Notification Service

## Purpose

The Notification Service is a pure consumer — it subscribes to domain events from all other services and delivers outbound notifications (email, SMS, push) to the relevant actors. It has no REST API that other services call; all its inputs arrive via Kafka. It acts as the single integration point with external notification providers.

## Owned Domain

Cross-cutting (no domain aggregate ownership)

## Tech Stack

| Component | Choice |
|-----------|--------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 (schema: `notification` — for delivery audit log) |
| Migrations | Flyway |
| Messaging | Spring Kafka |
| External APIs | Email provider (SMTP/SES), SMS provider (Twilio or equivalent) |
| Module | `com.logistics.notification` (JPMS) |

## SLA / Availability

| Metric | Target |
|--------|--------|
| Uptime | 99.5% |
| Notification delivery after event | < 30 seconds |
| Delivery audit retention | 90 days |

## Dependencies (via Kafka)

| Topic consumed | From service | Notification triggered |
|----------------|-------------|----------------------|
| `shipment.created` | Shipment Service | Confirm shipment receipt to Shipper |
| `shipment.assigned` | Shipment Service | Notify Driver of assignment; notify Shipper of ETA |
| `shipment.picked-up` | Shipment Service | Notify Shipper that cargo is in transit |
| `shipment.delivered` | Shipment Service | Notify Shipper of delivery confirmation |
| `shipment.cancelled` | Shipment Service | Notify Shipper and Carrier of cancellation |
| `billing.invoice-generated` | Billing Service | Send invoice to Shipper |
| `warehouse.capacity-updated` | Warehouse Service | Alert ops team when utilization ≥ 90% |

## External Dependencies

| System | Purpose |
|--------|---------|
| Email provider | Transactional emails to Shippers and Admins |
| SMS provider | Real-time status updates to Drivers |

---

## REST API

Base path: `/api/v1/notifications`

The Notification Service exposes a read-only REST API for audit purposes only. No other service calls it directly.

---

### GET `/api/v1/notifications`
List notification delivery records.

**Query parameters:**
- `recipientId` — actor ID (shipperId, driverId, etc.)
- `eventType` — filter by triggering event type
- `status` — `SENT | FAILED | PENDING`
- `page`, `size`

**Response `200 OK`:**
```json
{
  "content": [
    {
      "notificationId": "string (UUID)",
      "recipientId": "string (UUID)",
      "channel": "EMAIL | SMS | PUSH",
      "eventType": "string",
      "status": "SENT | FAILED | PENDING",
      "sentAt": "string (ISO-8601 UTC, nullable)",
      "failureReason": "string (nullable)"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1000
}
```

---

### GET `/api/v1/notifications/{notificationId}`
Retrieve a single notification delivery record.

**Response `200 OK`:**
```json
{
  "notificationId": "string (UUID)",
  "recipientId": "string (UUID)",
  "channel": "string",
  "eventType": "string",
  "subject": "string (nullable)",
  "body": "string",
  "status": "string",
  "sentAt": "string (ISO-8601 UTC, nullable)",
  "failureReason": "string (nullable)"
}
```

---

## Kafka Produced

None — Notification Service does not publish domain events.

## Kafka Consumed

| Topic | Notification sent |
|-------|------------------|
| `shipment.created` | Email to Shipper: shipment confirmed |
| `shipment.assigned` | SMS to Driver: assignment details + route; Email to Shipper: ETA |
| `shipment.picked-up` | Email to Shipper: cargo picked up |
| `shipment.delivered` | Email to Shipper: delivery confirmed |
| `shipment.cancelled` | Email to Shipper and Carrier |
| `billing.invoice-generated` | Email to Shipper with invoice PDF link |
| `warehouse.capacity-updated` | Email to ops team when utilization ≥ 90% |
