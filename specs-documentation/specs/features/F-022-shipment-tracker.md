# F-022 — Shipment Tracker

**Epic:** EP-016 (Shipper Portal)
**Status:** Planned

---

## Goal

Give Shippers a real-time list of their shipments with status filters and a detail view showing the full lifecycle timeline, cargo specs, and route summary.

## Actors

- Shipper — views and filters their own shipments
- Platform — supplies data via shipment-service (port 8081) and routing-service (port 8084)

## Preconditions

- Actor authenticated with role SHIPPER
- shipment-service is reachable

## Workflow

### List page (`/shipments`)

1. Shipper navigates to `/shipments`
2. Page renders a tab bar: All | Created | Scheduled | Assigned | In Transit | Delivered | Cancelled
3. Shipment list shows: shipment ID (truncated), origin → destination, SLA type badge, status badge, required delivery date
4. TanStack Query refetches every 15 seconds (background refresh)
5. Shipper clicks a row → navigates to `/shipments/[id]`

### Detail page (`/shipments/[id]`)

1. Shipment detail renders: status timeline, cargo spec table, origin/destination addresses, route summary (distance, ETA, fuel estimate)
2. Status timeline shows each status transition with timestamp
3. If status is ASSIGNED: vehicle plate and driver name shown
4. If status is DELIVERED: actual delivery date vs promised date; SLA result badge (On Time / Late)
5. If route is available: distance, estimated duration, fuel cost, toll cost from routing-service

## Business Rules Referenced

- Shipment lifecycle: CREATED → SCHEDULED → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED
- SLA types: STANDARD / PRIORITY / EXPRESS displayed as color-coded badges

## Edge Cases

- Shipment not found → 404 page with "Back to list" button
- routing-service unavailable → route section shows "Route data unavailable" without breaking the rest of the page
- Shipment in terminal status (DELIVERED, CANCELLED) → no background polling

## Acceptance Criteria

- **AC-022-01:** Shipment list shows only the authenticated Shipper's shipments
- **AC-022-02:** Status filter tabs correctly filter the list
- **AC-022-03:** List refreshes in background every 15 seconds without page reload
- **AC-022-04:** Detail page shows complete status timeline with timestamps
- **AC-022-05:** SLA result badge is green for on-time, red for late deliveries

## Telemetry

- Tab filter usage tracked (most-used status filter)
- Time on shipment detail page
