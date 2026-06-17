# User Stories — EP-016 Shipper Portal

All stories are for the **Shipper** actor. Priority: Must / Should / Could.

---

## Authentication

| ID | Story | Priority |
|----|-------|----------|
| US-EP016-001 | As a Shipper, I want to log in with my email and password so that I can access my shipment data securely. | Must |
| US-EP016-002 | As a Shipper, I want my session to expire after 8 hours so that unauthorised users cannot access my account on a shared device. | Must |
| US-EP016-003 | As a Shipper, I want to log out explicitly so that my session is invalidated immediately. | Must |

---

## Dashboard

| ID | Story | Priority |
|----|-------|----------|
| US-EP016-004 | As a Shipper, I want to see a summary of my active shipments on the dashboard so that I understand the current state of my operations at a glance. | Must |
| US-EP016-005 | As a Shipper, I want to see my outstanding invoice total on the dashboard so that I know how much I owe without navigating to the billing page. | Should |

---

## Shipment Tracker

| ID | Story | Priority |
|----|-------|----------|
| US-EP016-006 | As a Shipper, I want to see a list of all my shipments so that I can track every order I have placed. | Must |
| US-EP016-007 | As a Shipper, I want to filter shipments by status (e.g., In Transit, Delivered) so that I can focus on shipments that need attention. | Must |
| US-EP016-008 | As a Shipper, I want the shipment list to refresh automatically every 15 seconds so that I do not have to reload the page to see status changes. | Should |
| US-EP016-009 | As a Shipper, I want to click a shipment to see its full lifecycle timeline so that I can understand exactly when each status change occurred. | Must |
| US-EP016-010 | As a Shipper, I want to see the assigned vehicle plate and driver name on a shipment detail so that I know who is handling my delivery. | Should |
| US-EP016-011 | As a Shipper, I want to see the route distance, ETA, and fuel cost on a shipment detail so that I understand the logistics of my delivery. | Should |
| US-EP016-012 | As a Shipper, I want to see a green "On Time" or red "Late" badge on delivered shipments so that I can immediately identify SLA violations. | Must |

---

## Billing

| ID | Story | Priority |
|----|-------|----------|
| US-EP016-013 | As a Shipper, I want to see a list of all my invoices so that I can track my charges. | Must |
| US-EP016-014 | As a Shipper, I want invoices with SLA penalties highlighted in red so that I can immediately spot unexpected charges. | Must |
| US-EP016-015 | As a Shipper, I want to see the itemised breakdown of an invoice (base cost + penalty) so that I can verify each line item. | Must |
| US-EP016-016 | As a Shipper, I want to filter invoices by status (Pending, Paid, Overdue) so that I can prioritise outstanding payments. | Should |
| US-EP016-017 | As a Shipper, I want overdue invoices highlighted in amber so that I am aware of payment urgency. | Should |
