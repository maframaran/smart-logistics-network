# Billing Service

## Purpose

The Billing Service handles all financial operations: shipper invoicing, SLA penalty calculation, and carrier payment processing. It reacts to delivery events from Kafka, computes charges based on route costs and SLA compliance, integrates with an external Payment Gateway, and publishes billing events for downstream notification.

## Owned Domain

Billing (see `architecture/domains.md`)

## Tech Stack

| Component | Choice |
|-----------|--------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 (schema: `billing`) |
| Migrations | Flyway |
| Messaging | Spring Kafka |
| External API | Payment Gateway (adapter, provider TBD) |
| Module | `com.logistics.billing` (JPMS) |

## SLA / Availability

| Metric | Target |
|--------|--------|
| Uptime | 99.9% |
| Invoice generation after delivery | < 1 minute |
| Carrier payment approval | < 24 hours |
| REST P99 latency | < 200ms |

## Dependencies (via Kafka)

| Topic consumed | From service | Purpose |
|----------------|-------------|---------|
| `shipment.delivered` | Shipment Service | Trigger invoice generation and carrier payment |
| `routing.route-calculated` | Routing Service | Store fuel/toll costs for invoice line items |

## External Dependencies

| System | Purpose |
|--------|---------|
| Payment Gateway | Process carrier payment transfers |

---

## REST API

Base path: `/api/v1/billing`

---

### GET `/api/v1/billing/invoices/{invoiceId}`
Retrieve an invoice.

**Response `200 OK`:**
```json
{
  "invoiceId": "string (UUID)",
  "shipmentId": "string (UUID)",
  "shipperId": "string (UUID)",
  "status": "DRAFT | ISSUED | PAID | DISPUTED | CANCELLED",
  "lineItems": [
    {
      "description": "string",
      "quantity": "number",
      "unitPriceEur": "number",
      "totalEur": "number"
    }
  ],
  "totalAmountEur": "number",
  "issuedAt": "string (ISO-8601 UTC)"
}
```

---

### GET `/api/v1/billing/invoices`
List invoices with filters.

**Query parameters:**
- `shipmentId`
- `shipperId`
- `status`
- `page`, `size`

**Response `200 OK`:**
```json
{
  "content": [
    {
      "invoiceId": "string (UUID)",
      "shipmentId": "string (UUID)",
      "totalAmountEur": "number",
      "status": "string",
      "issuedAt": "string (ISO-8601 UTC)"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 500
}
```

---

### POST `/api/v1/billing/invoices/{invoiceId}/waive-penalty`
Waive the SLA penalty on an invoice (Platform Admin only).

**Request body:**
```json
{
  "reason": "string"
}
```

**Response `200 OK`:**
```json
{
  "invoiceId": "string (UUID)",
  "penaltyWaived": true,
  "previousPenaltyEur": "number",
  "newTotalAmountEur": "number",
  "waivedBy": "string (adminId)",
  "waivedAt": "string (ISO-8601 UTC)"
}
```

---

### GET `/api/v1/billing/payments/{paymentId}`
Retrieve a carrier payment.

**Response `200 OK`:**
```json
{
  "paymentId": "string (UUID)",
  "carrierId": "string (UUID)",
  "shipmentId": "string (UUID)",
  "amountEur": "number",
  "status": "PENDING | APPROVED | PAID | FAILED",
  "createdAt": "string (ISO-8601 UTC)",
  "paidAt": "string (ISO-8601 UTC, nullable)"
}
```

---

### GET `/api/v1/billing/payments`
List carrier payments with filters.

**Query parameters:**
- `carrierId`
- `status`
- `page`, `size`

**Response `200 OK`:**
```json
{
  "content": [
    {
      "paymentId": "string (UUID)",
      "shipmentId": "string (UUID)",
      "amountEur": "number",
      "status": "string"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 200
}
```

---

### GET `/api/v1/billing/config`
Retrieve current billing configuration (commission rate, penalty caps).

**Response `200 OK`:**
```json
{
  "platformCommissionPercent": "number",
  "penaltyCap": "FULL_BASE_COST",
  "penaltyRates": {
    "STANDARD": 0.05,
    "PRIORITY": 0.15,
    "EXPRESS": 0.25
  }
}
```

---

### PUT `/api/v1/billing/config`
Update billing configuration (Platform Admin only).

**Request body:**
```json
{
  "platformCommissionPercent": "number (0–100)"
}
```

---

## Kafka Produced

| Topic | When |
|-------|------|
| `billing.invoice-generated` | Invoice created in ISSUED status |

## Kafka Consumed

| Topic | Action |
|-------|--------|
| `shipment.delivered` | Generate invoice + create carrier payment |
| `routing.route-calculated` | Cache route cost data for invoice computation |
