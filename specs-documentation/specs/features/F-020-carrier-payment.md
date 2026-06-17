# Feature: F-020 — Carrier Payment

**Epic:** EP-006 Billing
**Domain:** Billing

## Goal

Approve and process payment to the Carrier for a completed shipment, deducting any applicable platform commission.

## Actors

- Platform (automated approval after delivery)
- Platform Administrator (manual approval for disputed payments)

## Preconditions

- Shipment has been delivered and invoiced
- Carrier has a registered payment account

## Workflow

1. Billing service creates `CarrierPayment` in PENDING status when `ShipmentDelivered` consumed
2. System calculates carrier payment: `baseCost - platformCommission%`
3. After configurable approval window (default: automatic within 24h), transition to APPROVED
4. Billing service calls Payment Gateway adapter to transfer funds
5. On success: transition to PAID, publish `CarrierPaymentApproved`
6. On failure: transition to FAILED, retry up to 3 times, alert billing ops

## Edge Cases

- EC-001: Payment Gateway returns error → retry 3× with backoff; mark FAILED after exhaustion
- EC-002: Carrier payment account not configured → block payment, notify carrier to add banking details
- EC-003: Shipment is disputed by shipper → hold carrier payment pending dispute resolution

## Acceptance Criteria

- AC-001: Carrier payment created in PENDING within 1 minute of `ShipmentDelivered`
- AC-002: Payment transitions to APPROVED within 24 hours automatically
- AC-003: `CarrierPaymentApproved` event published
- AC-004: Payment failures are retried and ops are alerted on exhaustion

## Telemetry

Track:
- `billing.carrier_payment.created` (with amountEur)
- `billing.carrier_payment.approved`
- `billing.carrier_payment.failed` (with reason)
