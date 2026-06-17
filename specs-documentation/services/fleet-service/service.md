# Fleet Service

## Purpose

The Fleet Service manages the vehicle registry for the platform. Carriers register their vehicles, update operational status, and the service provides eligibility queries used by the Shipment Service during assignment. It is the sole authoritative source for vehicle capacity, type, and current status.

## Owned Domain

Fleet — Vehicle aggregate (see `architecture/domains.md`)

## Tech Stack

| Component | Choice |
|-----------|--------|
| Runtime | Java 21 (virtual threads enabled) |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 (schema: `fleet`) |
| Migrations | Flyway |
| Messaging | Spring Kafka |
| Module | `com.logistics.fleet` (JPMS) |

## SLA / Availability

| Metric | Target |
|--------|--------|
| Uptime | 99.9% |
| REST P99 latency | < 150ms |
| Status update event publishing | < 100ms after change |

## Dependencies (via Kafka)

None — Fleet Service is a producer only. It does not consume events from other services.

---

## REST API

Base path: `/api/v1/vehicles`

---

### POST `/api/v1/vehicles`
Register a new vehicle.

**Request body:**
```json
{
  "carrierId": "string (UUID)",
  "plateNumber": "string",
  "type": "TRUCK | VAN | REFRIGERATED_TRUCK | HAZMAT_TRUCK",
  "capacity": {
    "maxWeightKg": "number (positive integer)",
    "maxVolumeM3": "number (positive)"
  },
  "fuelType": "DIESEL | ELECTRIC | HYBRID",
  "refrigerated": "boolean",
  "hazmatCertified": "boolean",
  "currentLocation": {
    "lat": "number",
    "lon": "number"
  }
}
```

**Response `201 Created`:**
```json
{
  "vehicleId": "string (UUID)",
  "status": "AVAILABLE",
  "createdAt": "string (ISO-8601 UTC)"
}
```

**Error responses:**
- `400` — `INVALID_CAPACITY`, `INCONSISTENT_VEHICLE_TYPE`
- `409` — `VEHICLE_ALREADY_EXISTS`

---

### PATCH `/api/v1/vehicles/{vehicleId}/status`
Update vehicle operational status.

**Request body:**
```json
{
  "status": "AVAILABLE | MAINTENANCE | OUT_OF_SERVICE",
  "reason": "string (optional)"
}
```

**Response `200 OK`:**
```json
{
  "vehicleId": "string (UUID)",
  "previousStatus": "string",
  "newStatus": "string",
  "updatedAt": "string (ISO-8601 UTC)"
}
```

**Error responses:**
- `409` — `INVALID_STATUS_TRANSITION` with `allowedTransitions: [...]`
- `404` — vehicle not found

---

### GET `/api/v1/vehicles/{vehicleId}`
Retrieve vehicle details.

**Response `200 OK`:**
```json
{
  "vehicleId": "string (UUID)",
  "carrierId": "string (UUID)",
  "plateNumber": "string",
  "type": "string",
  "capacity": { "maxWeightKg": "number", "maxVolumeM3": "number" },
  "fuelType": "string",
  "refrigerated": "boolean",
  "hazmatCertified": "boolean",
  "status": "string",
  "currentLocation": { "lat": "number", "lon": "number" }
}
```

---

### GET `/api/v1/vehicles`
Query vehicles with filters.

**Query parameters:**
- `status` — filter by VehicleStatus
- `carrierId` — filter by carrier
- `minWeightKg` — minimum weight capacity
- `minVolumeM3` — minimum volume capacity
- `refrigerated` — boolean filter
- `page`, `size`

**Response `200 OK`:**
```json
{
  "content": [
    {
      "vehicleId": "string (UUID)",
      "type": "string",
      "status": "string",
      "capacity": { "maxWeightKg": "number", "maxVolumeM3": "number" },
      "refrigerated": "boolean"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50
}
```

---

## Kafka Produced

| Topic | When |
|-------|------|
| `fleet.vehicle-status-changed` | Any vehicle status transition |

## Kafka Consumed

None.
