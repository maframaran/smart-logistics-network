# Feature: F-014 — Optimize Route

**Epic:** EP-004 Route Optimization
**Domain:** Routing

## Goal

Given multiple candidate routes for a shipment, select and return the optimal route minimizing a weighted combination of travel time, fuel consumption, and toll costs.

## Actors

- Platform (triggered by assignment use case or recalculation events)

## Preconditions

- At least one viable route exists for the shipment
- Optimization weights are configured (default: minimize cost)

## Workflow

1. Routing service receives optimization request with shipmentId and vehicleType
2. Service requests multiple route alternatives from Maps API (e.g. fastest, cheapest, balanced)
3. Service scores each route using the optimization function:
   - Score = α × normalizedTime + β × normalizedFuelCost + γ × normalizedTollCost
   - Default weights: α=0.3, β=0.4, γ=0.3 (configurable per SLA type)
4. Service selects the route with the lowest score
5. Publishes `RouteCalculated` event with the selected route

## Business Rules

- EXPRESS SLA: weight shifts toward minimizing time (α=0.7, β=0.2, γ=0.1)
- STANDARD SLA: balance cost and time (default weights)
- Route must still satisfy ETA ≤ promisedDeliveryDate after optimization

## Edge Cases

- EC-001: Only one route available → return it without scoring
- EC-002: Optimized route exceeds SLA ETA → fall back to fastest route; if still infeasible, signal `SLA_INFEASIBLE`

## Acceptance Criteria

- AC-001: Optimized route has lower or equal cost vs. non-optimized alternative
- AC-002: EXPRESS shipments receive time-optimized routes
- AC-003: Optimization weights are configurable without redeployment

## Telemetry

Track:
- `route.optimization.executed` (with slaType, selectedRouteCostEur)
- `route.optimization.fallback_to_fastest`
