# EP-017 — Carrier Portal

**Phase:** 2 (UI layer for Phase 1 & 2 backend)
**Status:** Planned

---

## Goal

Provide Carriers with a web portal to manage their fleet (vehicles and drivers), monitor warehouse capacity, and review assigned shipments — giving full operational visibility without API calls.

## Actors

- **Carrier** — primary user; authenticated via username/password (Phase 2) or SSO (Phase 5)
- **Platform** — backend services supplying data

## Epics This Depends On

- EP-002 Fleet Management (backend)
- EP-003 Driver Management (backend)
- EP-005 Warehouse Management (backend)
- EP-001 Shipment Management (backend — assigned shipments view)

## Features

| ID | Feature |
|----|---------|
| F-021 | Dashboard Overview |
| F-023 | Fleet Board |
| F-024 | Warehouse Dashboard |

## User Stories

See `specs/user-stories/US-EP017-carrier-portal.md`

## Acceptance Criteria Summary

- Carrier can log in and see only their own vehicles and drivers
- Fleet board shows real-time availability status (AVAILABLE / ASSIGNED / MAINTENANCE)
- Warehouse capacity gauges update within 30 seconds of an inventory change
- Driver hours bar shows daily progress toward the 9-hour BR-005 limit

## Non-Functional Requirements

| Concern | Target |
|---------|--------|
| Auth | httpOnly JWT cookie; role = CARRIER in session |
| Data scope | Carrier sees only vehicles/drivers where `carrierId = session.userId` |
| Availability | 99.5% uptime |
| Accessibility | WCAG 2.1 AA |
