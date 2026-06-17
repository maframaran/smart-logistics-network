# Topic: `routing.route-calculated`

## Purpose

Published when the Routing Service successfully calculates and optimizes a route for a shipment. The Shipment Service uses this to complete pending assignments (supplying ETA); the Billing Service caches fuel and toll costs for invoice computation.

## Configuration

| Property | Value |
|----------|-------|
| Partitions | 12 |
| Replication factor | 3 (production) |
| Retention | 7 days |
| Partition key | `shipmentId` |
| Dead-letter topic | `routing.route-calculated.dlq` |
| Consumer group (shipment) | `shipment-service.routing.route-calculated` |
| Consumer group (billing) | `billing-service.routing.route-calculated` |

## Producer

`routing-service` — published after route calculation and optimization completes.

## Consumers

| Service | Action |
|---------|--------|
| `shipment-service` | Store routeId and etaAt; unblock pending assignment decision |
| `billing-service` | Cache totalDistanceKm, fuelEstimateL, tollCostEur for future invoice |

## Payload Schema

```json
{
  "eventId": "string (UUID)",
  "eventVersion": "integer",
  "eventType": "RouteCalculated",
  "occurredAt": "string (ISO-8601 UTC)",
  "routeId": "string (UUID)",
  "shipmentId": "string (UUID)",
  "vehicleType": "TRUCK | VAN | REFRIGERATED_TRUCK | HAZMAT_TRUCK",
  "slaType": "STANDARD | PRIORITY | EXPRESS",
  "totalDistanceKm": "number",
  "estimatedDurationMin": "number",
  "etaAt": "string (ISO-8601 UTC)",
  "fuelEstimateL": "number",
  "tollCostEur": "number",
  "optimizationScore": "number — weighted score used for route selection",
  "calculatedAt": "string (ISO-8601 UTC)"
}
```

## Example Payload

```json
{
  "eventId": "e5f6a7b8-0000-0000-0000-000000000001",
  "eventVersion": 1,
  "eventType": "RouteCalculated",
  "occurredAt": "2026-06-16T09:03:00Z",
  "routeId": "e2f5a4d6-1111-2222-3333-777788889999",
  "shipmentId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "vehicleType": "TRUCK",
  "slaType": "STANDARD",
  "totalDistanceKm": 584.2,
  "estimatedDurationMin": 330,
  "etaAt": "2026-06-17T14:30:00Z",
  "fuelEstimateL": 87.6,
  "tollCostEur": 32.50,
  "optimizationScore": 0.42,
  "calculatedAt": "2026-06-16T09:03:00Z"
}
```
