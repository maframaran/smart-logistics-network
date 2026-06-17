# EP-016 — Shipper Portal

**Phase:** 2 (UI layer for Phase 1 & 2 backend)
**Status:** Planned

---

## Goal

Provide Shippers with a web portal to create and track their shipments, review invoices, manage SLA expectations, and receive delivery confirmations — without needing to call REST APIs directly.

## Actors

- **Shipper** — primary user; authenticated via username/password (Phase 2) or SSO (Phase 5)
- **Platform** — backend services supplying data

## Epics This Depends On

- EP-001 Shipment Management (backend)
- EP-006 Billing (backend)
- EP-007 Event-Driven Architecture (notifications)

## Features

| ID | Feature |
|----|---------|
| F-021 | Dashboard Overview |
| F-022 | Shipment Tracker |
| F-025 | Billing Dashboard |

## User Stories

See `specs/user-stories/US-EP016-shipper-portal.md`

## Acceptance Criteria Summary

- Shipper can log in and see only their own shipments
- Shipment list updates within 15 seconds of a status change
- Invoice table highlights SLA penalties in red
- All pages render within 2 seconds on a 10 Mbps connection

## Non-Functional Requirements

| Concern | Target |
|---------|--------|
| Auth | httpOnly JWT cookie; session expires in 8 hours |
| Data scope | Shipper sees only shipments where `shipperId = session.userId` |
| Availability | Inherits backend SLAs; UI itself 99.5% uptime |
| Accessibility | WCAG 2.1 AA — shadcn/ui primitives are ARIA-compliant |
