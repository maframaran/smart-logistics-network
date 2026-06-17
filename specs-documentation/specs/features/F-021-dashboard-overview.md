# F-021 — Dashboard Overview

**Epic:** EP-016 (Shipper), EP-017 (Carrier)
**Status:** Planned

---

## Goal

Show a single-page summary of the platform's operational state for the authenticated actor: shipment counts by status, fleet availability, warehouse fill levels, and outstanding invoices.

## Actors

- Shipper — sees their shipment stats and invoice summary
- Carrier — sees their fleet stats and warehouse summary

## Preconditions

- Actor is authenticated
- At least one backend service is reachable

## Workflow

1. Dashboard page loads — four stat cards render immediately with loading skeletons
2. Next.js Server Components fetch summary data in parallel from all relevant services
3. Cards populate: Active Shipments, Available Vehicles, Warehouse Fill %, Outstanding Invoices
4. Each card links to its corresponding detail page

## Business Rules Referenced

None — read-only aggregation; no mutations on this page.

## Edge Cases

- If a backend service is unreachable, that card shows an error state with a retry button; other cards render normally
- Empty state (no data yet): cards show "0" with a "Get started" call-to-action

## Acceptance Criteria

- **AC-021-01:** Dashboard loads in < 2s with all four cards populated
- **AC-021-02:** Each stat is scoped to the authenticated actor's data
- **AC-021-03:** Unreachable service shows error card without breaking other cards
- **AC-021-04:** Active Shipments count matches shipment-service list filtered by non-terminal statuses

## Telemetry

- Page load time tracked via `performance.now()`
- Per-service fetch duration logged server-side
