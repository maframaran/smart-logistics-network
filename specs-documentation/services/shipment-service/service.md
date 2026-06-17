# Shipment Service

## Purpose

The Shipment Service is the core orchestrator of the platform. It manages the full lifecycle of a shipment from creation through delivery, enforcing all status transitions, business rules, and SLA constraints. It coordinates with Fleet, Routing, Billing, and Notification services exclusively through Kafka events.

## Owned Domain

Shipment (see `architecture/domains.md`)

## Tech Stack

| Component | Choice |
|-----------|--------|
| Runtime | Java 21 (virtual threads enabled) |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 (schema: `shipment`) |
| Migrations | Flyway |
| Messaging | Spring Kafka |
| Module | `com.logistics.shipment` (JPMS) |

## SLA / Availability

| Metric | Target |
|--------|--------|
| Uptime | 99.9% |
| REST P99 latency | < 200ms |
| Assignment time | < 2 minutes (95th percentile) |
| Event publishing latency | < 100ms after state change |

## Dependencies (via Kafka)

| Topic consumed | From service | Purpose |
|----------------|-------------|---------|
| `routing.route-calculated` | Routing Service | Receive ETA and cost for assignment feasibility |
| `fleet.vehicle-status-changed` | Fleet Service | Update vehicle availability for assignment |
| `fleet.driver-status-changed` | Driver Service | Update driver availability for assignment |

---

## REST API

Base path: `/api/v1/shipments`

---

### POST `/api/v1/shipments`
Create a new shipment.

**Request body:**
```json
{
  "origin": {
    "street": "string",
    "city": "string",
    "country": "string (ISO 3166-1 alpha-2)"
  },
  "destination": {
    "street": "string",
    "city": "string",
    "country": "string (ISO 3166-1 alpha-2)"
  },
  "cargo": {
    "weightKg": "number (positive)",
    "volumeM3": "number (positive)",
    "requiresHazmat": "boolean",
    "requiresColdChain": "boolean"
  },
  "slaType": "STANDARD | PRIORITY | EXPRESS",
  "requiredDeliveryDate": "string (ISO-8601 UTC)"
}
```

**Response `201 Created`:**
```json
{
  "shipmentId": "string (UUID)",
  "status": "CREATED",
  "createdAt": "string (ISO-8601 UTC)"
}
```

**Error responses:**
- `400` — `INVALID_CARGO_SPEC`, `INVALID_DELIVERY_DATE`, `GEOCODING_FAILED`

---

### POST `/api/v1/shipments/{shipmentId}/schedule`
Schedule a pickup window for a created shipment.

**Request body:**
```json
{
  "pickupWindowStart": "string (ISO-8601 UTC)",
  "pickupWindowEnd": "string (ISO-8601 UTC)"
}
```

**Response `200 OK`:**
```json
{
  "shipmentId": "string (UUID)",
  "status": "SCHEDULED",
  "pickupWindowStart": "string (ISO-8601 UTC)",
  "pickupWindowEnd": "string (ISO-8601 UTC)"
}
```

**Error responses:**
- `400` — `PICKUP_WINDOW_TOO_SOON`, `WAREHOUSE_CLOSED`, `INVALID_STATUS_TRANSITION`
- `404` — shipment not found

---

### POST `/api/v1/shipments/{shipmentId}/assign`
Trigger automated assignment or submit a manual override.

**Request body (manual override only — omit for automated):**
```json
{
  "vehicleId": "string (UUID)",
  "driverId": "string (UUID)",
  "override": true,
  "overrideReason": "string"
}
```

**Response `200 OK`:**
```json
{
  "shipmentId": "string (UUID)",
  "status": "ASSIGNED",
  "vehicleId": "string (UUID)",
  "driverId": "string (UUID)",
  "routeId": "string (UUID)",
  "etaAt": "string (ISO-8601 UTC)"
}
```

**Error responses:**
- `409` — `NO_VEHICLES_AVAILABLE`, `NO_DRIVERS_AVAILABLE`, `SLA_INFEASIBLE`, `CAPACITY_WEIGHT_EXCEEDED`, `CAPACITY_VOLUME_EXCEEDED`, `HAZMAT_CERTIFICATION_REQUIRED`, `COLD_CHAIN_REQUIRED`, `DRIVER_HOURS_EXCEEDED`

---

### POST `/api/v1/shipments/{shipmentId}/cancel`
Request cancellation of a shipment.

**Request body:**
```json
{
  "reason": "string (optional)"
}
```

**Response `200 OK`:**
```json
{
  "shipmentId": "string (UUID)",
  "status": "CANCELLED | PENDING_APPROVAL",
  "cancellationFeeApplied": "boolean",
  "message": "string"
}
```

**Error responses:**
- `409` — `CANCELLATION_FORBIDDEN` (IN_TRANSIT or DELIVERED)

---

### GET `/api/v1/shipments/{shipmentId}`
Retrieve shipment details and current status.

**Response `200 OK`:**
```json
{
  "shipmentId": "string (UUID)",
  "status": "string (ShipmentStatus)",
  "origin": { "street": "string", "city": "string", "country": "string" },
  "destination": { "street": "string", "city": "string", "country": "string" },
  "cargo": {
    "weightKg": "number",
    "volumeM3": "number",
    "requiresHazmat": "boolean",
    "requiresColdChain": "boolean"
  },
  "slaType": "string",
  "requiredDeliveryDate": "string (ISO-8601 UTC)",
  "assignment": {
    "vehicleId": "string (UUID, nullable)",
    "driverId": "string (UUID, nullable)",
    "routeId": "string (UUID, nullable)",
    "etaAt": "string (ISO-8601 UTC, nullable)"
  },
  "createdAt": "string (ISO-8601 UTC)",
  "updatedAt": "string (ISO-8601 UTC)"
}
```

---

### GET `/api/v1/shipments`
List shipments with optional filters.

**Query parameters:**
- `status` — filter by ShipmentStatus
- `shipperId` — filter by shipper (future multi-tenant)
- `page`, `size` — pagination

**Response `200 OK`:**
```json
{
  "content": [ { "shipmentId": "...", "status": "...", "createdAt": "..." } ],
  "page": 0,
  "size": 20,
  "totalElements": 100
}
```

---

## Kafka Produced

| Topic | When |
|-------|------|
| `shipment.created` | Shipment transitions to CREATED |
| `shipment.assigned` | Shipment transitions to ASSIGNED |
| `shipment.picked-up` | Driver confirms pickup |
| `shipment.delivered` | Delivery confirmed |
| `shipment.cancelled` | Shipment cancelled |

## Kafka Consumed

| Topic | Action |
|-------|--------|
| `routing.route-calculated` | Store route ETA; complete pending assignment |
| `fleet.vehicle-status-changed` | Refresh vehicle availability cache |
| `fleet.driver-status-changed` | Refresh driver availability cache |
