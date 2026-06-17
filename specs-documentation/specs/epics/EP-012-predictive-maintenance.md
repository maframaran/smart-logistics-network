# Epic: EP-012 — Predictive Maintenance

**Phase:** 4
**Domain:** Fleet / AI

## Problem

Vehicle breakdowns during transit cause SLA violations, customer dissatisfaction, and costly emergency replacements. Reactive maintenance is expensive.

## Success Metrics

- 80% of vehicle failures predicted 48 hours in advance
- Unplanned breakdown rate reduced by 40%
- Maintenance scheduling cost reduced by 15%

## Features

- Vehicle telemetry ingestion (mileage, engine hours, fault codes)
- Failure probability model per vehicle
- Proactive maintenance work order generation
- Integration with fleet status (MAINTENANCE state triggered automatically)

## Domain Events Produced

- `MaintenanceAlertRaised`
- `VehicleStatusChanged` (→ MAINTENANCE)

## Dependencies

- [EP-002](EP-002-fleet-management.md)
- [EP-007](EP-007-event-driven-architecture.md)
