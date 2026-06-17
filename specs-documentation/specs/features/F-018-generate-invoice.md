# Feature: F-018 — Generate Invoice

**Epic:** EP-006 Billing
**Domain:** Billing

## Goal

Automatically generate a shipper invoice when a shipment is delivered, including base transportation cost, SLA penalty (if applicable), and any additional fees.

## Actors

- Platform (triggered by `ShipmentDelivered` event)

## Preconditions

- Shipment has been delivered (`ShipmentDelivered` event consumed)
- Route cost data is available (`RouteCalculated` event previously consumed)
- SLA type and promised delivery date are known

## Workflow

1. Billing service consumes `ShipmentDelivered` event
2. Service retrieves route cost data (fuel, tolls, distance) from `RouteCalculated` event
3. Service calculates base transportation cost (distance × rate + fuel + tolls)
4. Service invokes SLA penalty calculation (F-019)
5. Service creates Invoice aggregate with line items: base cost + penalty (if any)
6. Invoice transitions to ISSUED status
7. Publishes `InvoiceGenerated` event

## Edge Cases

- EC-001: Route cost data not available (routing service was down) → generate invoice with base rate estimate; flag for manual review
- EC-002: Shipment delivered before promised date → no penalty, possible early delivery discount (configurable)
- EC-003: Invoice generation fails → retry up to 3 times; alert billing ops on exhaustion

## Acceptance Criteria

- AC-001: Invoice generated within 1 minute of `ShipmentDelivered` event
- AC-002: Invoice includes itemized line items (base cost, penalty, fees)
- AC-003: `InvoiceGenerated` event published
- AC-004: Shipper notified of invoice via Notification service

## Telemetry

Track:
- `billing.invoice.generated` (with totalAmountEur, hasPenalty)
- `billing.invoice.generation_failed` (with reason)
