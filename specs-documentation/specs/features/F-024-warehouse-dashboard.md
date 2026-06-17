# F-024 — Warehouse Capacity Dashboard

**Epic:** EP-017 (Carrier Portal)
**Status:** Planned

---

## Goal

Give Carriers real-time visibility into warehouse fill levels and per-SKU inventory, so they can proactively avoid capacity violations (BR-006) and plan inbound shipments.

## Actors

- Carrier / Warehouse Operator — views warehouse capacity
- Platform — supplies data via warehouse-service (port 8085)

## Preconditions

- Actor authenticated

## Workflow

### Warehouse List (`/warehouse`)

1. Page renders a card grid — one card per warehouse
2. Each card: warehouse name, location city, capacity gauge (current weight % of max, current volume % of max)
3. Gauge colour: green < 70%, amber 70–89%, red ≥ 90%
4. Card click → navigates to `/warehouse/[id]`
5. TanStack Query refetches every 30 seconds

### Warehouse Detail (`/warehouse/[id]`)

1. Header: name, location, max weight (kg), max volume (m³)
2. Two gauges: Weight fill % and Volume fill %
3. Inventory table: SKU | Quantity | Weight/unit | Volume/unit | Expiration date
4. Table sortable by quantity (desc) and expiration date (asc)
5. SKUs expiring within 7 days highlighted in amber

## Business Rules Referenced

- BR-006: `currentWeight + incoming ≤ maxWeight` — gauge turns red at ≥ 90% to signal risk
- Expired inventory is not shown (filtered client-side by `expirationDate < today`)

## Edge Cases

- Warehouse at 100% fill → gauge is solid red; "Receive Inventory" button disabled
- No warehouses registered → empty state
- SKU with no expiration date → expiration column shows "—"

## Acceptance Criteria

- **AC-024-01:** Capacity gauge reflects the current weight and volume fill percentages
- **AC-024-02:** Gauge turns red when fill ≥ 90%
- **AC-024-03:** Inventory table shows all current SKUs with quantities
- **AC-024-04:** SKUs expiring within 7 days are highlighted amber
- **AC-024-05:** Gauges refresh within 30 seconds of an inventory receipt

## Telemetry

- Warehouse detail page views per warehouse ID
- Time spent on near-capacity warehouses (fill ≥ 90%)
