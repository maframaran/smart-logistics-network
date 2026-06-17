# Warehouse Service

## Purpose

The Warehouse Service manages warehouse configuration, inventory receipt, and outbound shipment preparation. It enforces warehouse capacity limits (BR-006), provides real-time utilization data, and publishes capacity change events so other services (Routing, Notification) stay informed about warehouse state.

## Owned Domain

Warehouse (see `architecture/domains.md`)

## Tech Stack

| Component | Choice |
|-----------|--------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 (schema: `warehouse`) |
| Migrations | Flyway |
| Messaging | Spring Kafka |
| Module | `com.logistics.warehouse` (JPMS) |

## SLA / Availability

| Metric | Target |
|--------|--------|
| Uptime | 99.9% |
| REST P99 latency | < 200ms |
| Capacity check | < 50ms |

## Dependencies (via Kafka)

| Topic consumed | From service | Purpose |
|----------------|-------------|---------|
| `shipment.assigned` | Shipment Service | Notify warehouse to prepare outbound shipment |

---

## REST API

Base path: `/api/v1/warehouses`

---

### POST `/api/v1/warehouses`
Register a new warehouse.

**Request body:**
```json
{
  "name": "string",
  "location": {
    "street": "string",
    "city": "string",
    "country": "string (ISO 3166-1 alpha-2)"
  },
  "maxUnits": "integer (positive)",
  "operatingHours": {
    "open": "string (HH:mm)",
    "close": "string (HH:mm)",
    "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"]
  }
}
```

**Response `201 Created`:**
```json
{
  "warehouseId": "string (UUID)",
  "currentUnits": 0,
  "maxUnits": "integer",
  "createdAt": "string (ISO-8601 UTC)"
}
```

---

### PATCH `/api/v1/warehouses/{warehouseId}`
Update warehouse capacity or operating hours.

**Request body:**
```json
{
  "maxUnits": "integer (optional)",
  "operatingHours": { "open": "string", "close": "string", "daysOfWeek": ["string"] }
}
```

**Response `200 OK`:**
```json
{
  "warehouseId": "string (UUID)",
  "maxUnits": "integer",
  "currentUnits": "integer",
  "utilizationPercent": "number"
}
```

**Error responses:**
- `409` — `CAPACITY_BELOW_CURRENT_STOCK`

---

### GET `/api/v1/warehouses/{warehouseId}`
Retrieve warehouse details and current utilization.

**Response `200 OK`:**
```json
{
  "warehouseId": "string (UUID)",
  "name": "string",
  "location": { "street": "string", "city": "string", "country": "string" },
  "maxUnits": "integer",
  "currentUnits": "integer",
  "utilizationPercent": "number",
  "operatingHours": { "open": "string", "close": "string", "daysOfWeek": ["string"] }
}
```

---

### POST `/api/v1/warehouses/{warehouseId}/inventory`
Record inbound inventory receipt.

**Request body:**
```json
{
  "sku": "string",
  "quantity": "integer (positive)",
  "batchNumber": "string (optional)",
  "expirationDate": "string (ISO-8601 date, optional)"
}
```

**Response `201 Created`:**
```json
{
  "inventoryId": "string (UUID)",
  "sku": "string",
  "quantity": "integer",
  "warehouseId": "string (UUID)",
  "currentUnits": "integer",
  "maxUnits": "integer",
  "receivedAt": "string (ISO-8601 UTC)"
}
```

**Error responses:**
- `409` — `WAREHOUSE_CAPACITY_EXCEEDED` with `alternatives: [{ warehouseId, availableUnits }]`
- `400` — `INVALID_EXPIRATION_DATE`

---

### POST `/api/v1/warehouses/{warehouseId}/outbound`
Confirm outbound inventory dispatch for a shipment.

**Request body:**
```json
{
  "shipmentId": "string (UUID)",
  "items": [
    { "sku": "string", "quantity": "integer", "batchNumber": "string (optional)" }
  ]
}
```

**Response `200 OK`:**
```json
{
  "shipmentId": "string (UUID)",
  "dispatchedItems": [{ "sku": "string", "quantity": "integer" }],
  "currentUnits": "integer",
  "dispatchedAt": "string (ISO-8601 UTC)"
}
```

**Error responses:**
- `409` — `INSUFFICIENT_STOCK` with `{ sku, available, requested }`

---

### GET `/api/v1/warehouses/{warehouseId}/inventory`
List current inventory for a warehouse.

**Query parameters:**
- `sku` — filter by SKU
- `page`, `size`

**Response `200 OK`:**
```json
{
  "content": [
    {
      "sku": "string",
      "quantity": "integer",
      "batchNumber": "string",
      "expirationDate": "string"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150
}
```

---

## Kafka Produced

| Topic | When |
|-------|------|
| `warehouse.capacity-updated` | After any inventory receipt or dispatch |

## Kafka Consumed

| Topic | Action |
|-------|--------|
| `shipment.assigned` | Queue outbound preparation task for warehouse operator |
