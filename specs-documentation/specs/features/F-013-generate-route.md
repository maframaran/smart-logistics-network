# Feature: F-013 — Generate Route

**Epic:** EP-004 Route Optimization
**Domain:** Routing

## Goal

Calculate a viable route from origin to destination for a given shipment and vehicle type, including distance, ETA, fuel estimate, and toll costs.

## Actors

- Platform (triggered by `ShipmentCreated` event via Kafka)

## Preconditions

- Origin and destination coordinates are available
- Vehicle type is known (affects road restrictions and fuel consumption model)

## Workflow

1. Routing service consumes `ShipmentCreated` event from Kafka
2. Service calls external Maps/Routing API adapter with origin, destination, vehicleType, deliveryWindow
3. API returns route segments, distance, ETA, fuel estimate, toll costs
4. Service creates Route aggregate and persists it
5. Service publishes `RouteCalculated` event with routeId, shipmentId, etaAt, totalDistanceKm, fuelEstimateL, tollCostEur

## Business Rules

- Route must comply with geographic constraints (weight-restricted roads for heavy vehicles, restricted zones)
- ETA calculated must be evaluated against SLA in the assignment use case (BR-004 enforced in F-005)

## Edge Cases

- EC-001: Maps API is unreachable → retry 3 times with exponential backoff; publish `RouteCalculationFailed` after exhaustion
- EC-002: No viable route exists (e.g. island without ferry schedule) → publish `RouteCalculationFailed` with `NO_ROUTE_FOUND`
- EC-003: Vehicle type exceeds road weight restrictions on all possible paths → `ROUTE_RESTRICTED_FOR_VEHICLE_TYPE`

## Acceptance Criteria

- AC-001: `RouteCalculated` event published within 5 seconds of receiving `ShipmentCreated`
- AC-002: Route includes total distance, ETA, fuel estimate, and toll cost
- AC-003: Route respects vehicle-type road restrictions

## Telemetry

Track:
- `route.generation.requested`
- `route.generation.succeeded` (with distanceKm, etaHours)
- `route.generation.failed` (with reason)
