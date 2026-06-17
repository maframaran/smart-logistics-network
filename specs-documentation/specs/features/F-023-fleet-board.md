# F-023 — Fleet Board

**Epic:** EP-017 (Carrier Portal)
**Status:** Planned

---

## Goal

Give Carriers a unified view of their vehicles and drivers — current status, capacity, certifications, and daily driving hours — so they can manage operations without calling APIs.

## Actors

- Carrier — views their own vehicles and drivers
- Platform — supplies data via fleet-service (port 8082) and driver-service (port 8083)

## Preconditions

- Actor authenticated with role CARRIER

## Workflow

### Fleet Board (`/fleet`)

1. Page renders two columns: Vehicles (left) and Drivers (right)
2. Vehicles: card per vehicle showing plate, type badge, capacity (weight/volume), status badge (AVAILABLE / ASSIGNED / MAINTENANCE / OUT_OF_SERVICE)
3. Drivers: card per driver showing name, license type, certifications (HAZMAT badge if certified), status badge, daily hours progress bar (BR-005: 0–9h)
4. Filter bar above each column: filter by status
5. TanStack Query refetches every 60 seconds

### Vehicle Detail (`/fleet/vehicles/[id]`)

- Plate, type, capacity, refrigeration flag, hazmat flag, current status, version (optimistic lock)
- Assignment history (if available from shipment-service)

### Driver Detail (`/fleet/drivers/[id]`)

- Name, license number, license type, certifications
- Daily hours bar: `hoursToday / 9` with colour coding (green < 7h, amber 7–8h, red > 8h)
- Status badge with transition timestamps

## Business Rules Referenced

- BR-005: Max 9h driving/day — visualised as a progress bar
- BR-003: HAZMAT certification badge shown when driver has `HAZMAT` in certifications
- BR-008: Refrigerated vehicle flag shown on REFRIGERATED_TRUCK cards

## Edge Cases

- No vehicles registered → empty state with "Register your first vehicle" call-to-action
- Driver at 9h limit → progress bar full red; status shown as RESTING
- driver-service unavailable → Drivers column shows error state; Vehicles column still renders

## Acceptance Criteria

- **AC-023-01:** Carrier sees only their own vehicles and drivers
- **AC-023-02:** Daily hours bar updates within 60 seconds of a driving session being recorded
- **AC-023-03:** HAZMAT badge visible on certified drivers
- **AC-023-04:** Driver at ≥ 9h shows red full progress bar and RESTING status
- **AC-023-05:** Status filter correctly hides non-matching cards

## Telemetry

- Driver hours bar view count (how often Carriers check hours)
