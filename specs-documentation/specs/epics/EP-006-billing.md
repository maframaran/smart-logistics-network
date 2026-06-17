# Epic: EP-006 — Billing

**Phase:** 2
**Domain:** Billing

## Problem

There is no automated invoicing for shippers, no SLA penalty calculation, and no carrier payment processing, requiring full manual billing operations.

## Success Metrics

- Invoice generated within 1 minute of shipment delivery
- 100% of SLA breaches result in a penalty line item
- Carrier payments approved within 24 hours of delivery confirmation

## Features

- F-018 Generate Invoice
- F-019 SLA Penalty Calculation
- F-020 Carrier Payment

## Business Rules

- BR-004 SLA penalty rates (Standard 5%, Priority 15%, Express 25%)

## SLA Types

| Type | Window | Penalty Rate |
|------|--------|-------------|
| STANDARD | 72h | 5% per hour late |
| PRIORITY | 24h | 15% per hour late |
| EXPRESS | 6h | 25% per hour late |

## Domain Events Produced

- `InvoiceGenerated`
- `InvoicePaid`
- `CarrierPaymentApproved`
- `SlaPenaltyApplied`

## External Dependencies

- Payment Gateway (adapter — see `architecture/context.md`)
