# Epic: EP-004 — Route Optimization

**Phase:** 2
**Domain:** Routing

## Problem

Shipment assignment lacks accurate ETAs and cost estimates. Routes are not optimized for fuel, tolls, or traffic, leading to SLA breaches and unnecessary costs.

## Success Metrics

- ETA accuracy within ±15 minutes for 90% of shipments
- 10% reduction in average fuel costs vs. unoptimized routing
- Route calculated in under 5 seconds

## Features

- F-013 Generate Route
- F-014 Optimize Route

## Business Rules

- BR-004 ETA must satisfy promised delivery date
- Geographic constraints (restricted zones, weight-restricted roads, border crossings)

## Domain Events Produced

- `RouteCalculated`
- `RouteRecalculated`

## External Dependencies

- Maps / Routing API (adapter — see `architecture/context.md`)
