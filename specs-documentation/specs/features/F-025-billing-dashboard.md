# F-025 — Billing Dashboard

**Epic:** EP-016 (Shipper Portal)
**Status:** Planned

---

## Goal

Give Shippers a clear view of their invoices, outstanding balances, and SLA penalty history so they can track costs and dispute incorrect charges.

## Actors

- Shipper — views their own invoices
- Platform — supplies data via billing-service (port 8086)

## Preconditions

- Actor authenticated with role SHIPPER

## Workflow

### Invoice List (`/billing`)

1. Page renders an invoice table: Invoice ID (truncated) | Shipment ID | Issued Date | SLA Type | Base Cost | Penalty | Total | Status badge (PENDING / PAID / OVERDUE)
2. Rows with `slaPenaltyBrl > 0` are highlighted with a red left border
3. Filter bar: All | Pending | Paid | Overdue
4. Sort by issued date (desc by default)
5. TanStack Query refetches every 5 minutes

### Invoice Detail (`/billing/[id]`)

1. Header: invoice number, issued date, shipment reference link
2. Line items: Base Transportation Cost | SLA Penalty (if applies) | Total
3. SLA penalty section: shows promised date, actual delivery date, hours late, penalty rate, calculated amount
4. Status badge + "Mark as Paid" button (PENDING only)
5. Carrier payment section: shows carrier portion after platform commission

## Business Rules Referenced

- SLA penalty rates: STANDARD BRL 50/day, PRIORITY BRL 150/day, EXPRESS BRL 300/day (see [ADR-017](../../adrs/ADR-017-sla-penalty-rates.md))
- Invoice status transitions: PENDING → PAID / OVERDUE (see billing-service domain)

## Edge Cases

- No invoices yet → empty state with "Your invoices will appear here after first delivery"
- OVERDUE invoice → status badge is red; row has amber background
- Invoice for cancelled shipment → shows cancellation fee line item instead of transportation cost

## Acceptance Criteria

- **AC-025-01:** Invoice list shows only the authenticated Shipper's invoices
- **AC-025-02:** Rows with SLA penalty > 0 have a red left border
- **AC-025-03:** OVERDUE invoices have amber background
- **AC-025-04:** Invoice detail shows itemised line items including penalty breakdown
- **AC-025-05:** Filter tabs correctly filter by payment status

## Telemetry

- Invoice detail views per invoice ID
- Click rate on invoices with SLA penalty (indicates dispute likelihood)
