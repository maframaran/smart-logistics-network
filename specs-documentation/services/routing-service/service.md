# Routing Service

## Purpose

The Routing Service calculates and optimizes transportation routes for shipments. It listens for `ShipmentCreated` events, calls an external Maps/Routing API, scores route alternatives based on SLA tier, and publishes the selected route back to the platform via `routing.route-calculated`. It acts as the anti-corruption layer between the external navigation API and the platform's domain model.

## Owned Domain

Routing (see `architecture/domains.md`)

## Tech Stack

| Component | Choice |
|-----------|--------|
| Runtime | Java 21 (virtual threads enabled — benefits from I/O-intensive Maps API calls) |
| Framework | Spring Boot 3.3 |
| Database | PostgreSQL 16 (schema: `routing`) |
| Migrations | Flyway |
| Messaging | Spring Kafka |
| External API | Maps/Routing API (adapter, provider TBD) |
| Module | `com.logistics.routing` (JPMS) |

## SLA / Availability

| Metric | Target |
|--------|--------|
| Uptime | 99.5% |
| Route calculation time | < 5 seconds (P99) |
| Event publishing after calculation | < 100ms |

## Dependencies (via Kafka)

| Topic consumed | From service | Purpose |
|----------------|-------------|---------|
| `shipment.created` | Shipment Service | Trigger route calculation for new shipments |

## External Dependencies

| System | Purpose |
|--------|---------|
| Maps / Routing API | Geocoding, road network data, traffic, route alternatives |

---

## REST API

Base path: `/api/v1/routes`

---

### GET `/api/v1/routes/{routeId}`
Retrieve a calculated route by ID.

**Response `200 OK`:**
```json
{
  "routeId": "string (UUID)",
  "shipmentId": "string (UUID)",
  "segments": [
    {
      "from": { "lat": "number", "lon": "number" },
      "to": { "lat": "number", "lon": "number" },
      "distanceKm": "number",
      "durationMin": "number"
    }
  ],
  "totalDistanceKm": "number",
  "estimatedDurationMin": "number",
  "etaAt": "string (ISO-8601 UTC)",
  "fuelEstimateL": "number",
  "tollCostEur": "number",
  "calculatedAt": "string (ISO-8601 UTC)"
}
```

**Error responses:**
- `404` — route not found

---

### GET `/api/v1/routes`
List routes for a shipment.

**Query parameters:**
- `shipmentId` — required
- `page`, `size`

**Response `200 OK`:**
```json
{
  "content": [
    {
      "routeId": "string (UUID)",
      "shipmentId": "string (UUID)",
      "totalDistanceKm": "number",
      "etaAt": "string (ISO-8601 UTC)",
      "tollCostEur": "number",
      "calculatedAt": "string (ISO-8601 UTC)"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1
}
```

---

### POST `/api/v1/routes/calculate`
Manually trigger route calculation (admin / testing use).

**Request body:**
```json
{
  "shipmentId": "string (UUID)",
  "origin": { "lat": "number", "lon": "number" },
  "destination": { "lat": "number", "lon": "number" },
  "vehicleType": "TRUCK | VAN | REFRIGERATED_TRUCK | HAZMAT_TRUCK",
  "slaType": "STANDARD | PRIORITY | EXPRESS",
  "requiredDeliveryDate": "string (ISO-8601 UTC)"
}
```

**Response `202 Accepted`:**
```json
{
  "message": "Route calculation triggered. Result will be published to routing.route-calculated."
}
```

---

## Kafka Produced

| Topic | When |
|-------|------|
| `routing.route-calculated` | Route successfully calculated and optimized |

## Kafka Consumed

| Topic | Action |
|-------|--------|
| `shipment.created` | Trigger route calculation for the new shipment |
