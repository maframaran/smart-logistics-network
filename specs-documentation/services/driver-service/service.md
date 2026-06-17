# Driver Service

## Purpose

The Driver Service manages driver registration, certifications, operational status, and working-hour compliance. Carriers register their drivers; the service enforces legal driving limits (BR-005) and publishes status changes so the Shipment Service can maintain an accurate pool of available drivers for assignment.

## Owned Domain

Fleet — Driver aggregate (see `architecture/domains.md`)

## Tech Stack

| Component | Choice |
|-----------|--------|
| Runtime | Java 21 (virtual threads enabled) |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 (schema: `driver`) |
| Migrations | Flyway |
| Messaging | Spring Kafka |
| Scheduler | Spring `@Scheduled` (daily hours reset at midnight) |
| Module | `com.logistics.driver` (JPMS) |

## SLA / Availability

| Metric | Target |
|--------|--------|
| Uptime | 99.9% |
| REST P99 latency | < 150ms |
| Hours compliance check | < 50ms |
| Daily reset job | Completes within 1 minute of midnight |

## Dependencies (via Kafka)

None — Driver Service is a producer only.

---

## REST API

Base path: `/api/v1/drivers`

---

### POST `/api/v1/drivers`
Register a new driver.

**Request body:**
```json
{
  "carrierId": "string (UUID)",
  "licenseNumber": "string",
  "licenseType": "B | C | CE",
  "certifications": ["HAZMAT", "FOOD_TRANSPORT", "OVERSIZED_LOAD"],
  "name": "string"
}
```

**Response `201 Created`:**
```json
{
  "driverId": "string (UUID)",
  "status": "AVAILABLE",
  "createdAt": "string (ISO-8601 UTC)"
}
```

**Error responses:**
- `400` — `INVALID_LICENSE_TYPE`
- `409` — `DRIVER_ALREADY_EXISTS`

---

### PATCH `/api/v1/drivers/{driverId}/status`
Update driver operational status.

**Request body:**
```json
{
  "status": "AVAILABLE | RESTING | SUSPENDED",
  "reason": "string (optional)"
}
```

**Response `200 OK`:**
```json
{
  "driverId": "string (UUID)",
  "previousStatus": "string",
  "newStatus": "string",
  "updatedAt": "string (ISO-8601 UTC)"
}
```

**Error responses:**
- `409` — `INVALID_STATUS_TRANSITION`
- `403` — `INSUFFICIENT_PERMISSIONS` (only admin can unsuspend)

---

### POST `/api/v1/drivers/{driverId}/hours`
Log a completed driving session.

**Request body:**
```json
{
  "sessionHours": "number (positive, max 9)"
}
```

**Response `200 OK`:**
```json
{
  "driverId": "string (UUID)",
  "sessionHours": "number",
  "dailyHoursToday": "number",
  "limitReached": "boolean",
  "newStatus": "string"
}
```

---

### GET `/api/v1/drivers/{driverId}`
Retrieve driver details and current hours.

**Response `200 OK`:**
```json
{
  "driverId": "string (UUID)",
  "carrierId": "string (UUID)",
  "licenseNumber": "string",
  "licenseType": "string",
  "certifications": ["string"],
  "status": "string",
  "workingHours": {
    "dailyHoursToday": "number",
    "weeklyHours": "number",
    "lastUpdatedAt": "string (ISO-8601 UTC)"
  }
}
```

---

### GET `/api/v1/drivers`
List drivers with filters.

**Query parameters:**
- `status` — filter by DriverStatus
- `carrierId`
- `certification` — filter by certification type
- `page`, `size`

**Response `200 OK`:**
```json
{
  "content": [
    {
      "driverId": "string (UUID)",
      "licenseType": "string",
      "certifications": ["string"],
      "status": "string",
      "dailyHoursToday": "number"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 30
}
```

---

## Kafka Produced

| Topic | When |
|-------|------|
| `fleet.driver-status-changed` | Any driver status transition |

## Kafka Consumed

None.
