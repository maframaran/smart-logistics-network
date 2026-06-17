# Feature: F-019 — SLA Penalty Calculation

**Epic:** EP-006 Billing
**Domain:** Billing

## Goal

Calculate the financial penalty owed to a Shipper when a shipment is delivered after the promised delivery date, based on the SLA type (BR-004).

## Actors

- Invoice Generation Use Case (internal, invoked by F-018)

## Preconditions

- Actual delivery timestamp is known
- Promised delivery date and SLA type are known
- Base transportation cost is calculated

## Workflow

1. Billing use case receives (actualDeliveredAt, promisedDeliveryDate, slaType, baseTransportCostEur)
2. System calculates delay: `hoursLate = max(0, (actualDeliveredAt - promisedDeliveryDate).toHours())`
3. If `hoursLate = 0`: return `SlaPenalty { amountEur = 0, reason = ON_TIME }`
4. Apply penalty rate by SLA type:
   - STANDARD: `penaltyEur = baseTransportCostEur × 0.05 × hoursLate`
   - PRIORITY: `penaltyEur = baseTransportCostEur × 0.15 × hoursLate`
   - EXPRESS: `penaltyEur = baseTransportCostEur × 0.25 × hoursLate`
5. Cap penalty at 100% of base transport cost
6. Return `SlaPenalty { amountEur, hoursLate, slaType, penaltyRate }`

## Business Rules

- BR-004: ETA ≤ promisedDeliveryDate; violation triggers penalty
- Penalty rates: STANDARD 5%, PRIORITY 15%, EXPRESS 25% (per hour late)
- Maximum penalty: 100% of base transport cost

## Edge Cases

- EC-001: `actualDeliveredAt < promisedDeliveryDate` → return zero penalty (on-time or early)
- EC-002: Base transport cost is zero (data error) → return zero penalty; flag for review
- EC-003: Delay caused by documented force majeure → flag for manual waiver review; still compute penalty in system

## Acceptance Criteria

- AC-001: On-time delivery returns zero penalty
- AC-002: Each SLA type applies the correct rate
- AC-003: Penalty is capped at 100% of base transport cost
- AC-004: `SlaPenaltyApplied` event published when penalty > 0

## Telemetry

Track:
- `billing.sla_penalty.calculated` (with slaType, hoursLate, penaltyAmountEur)
- `billing.sla_penalty.waived` (with reason)
