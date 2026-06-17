# User Stories — EP-017 Carrier Portal

All stories are for the **Carrier** actor. Priority: Must / Should / Could.

---

## Authentication

| ID | Story | Priority |
|----|-------|----------|
| US-EP017-001 | As a Carrier, I want to log in with my credentials so that I can access my operational data. | Must |
| US-EP017-002 | As a Carrier, I want my session to expire after 8 hours for security. | Must |

---

## Dashboard

| ID | Story | Priority |
|----|-------|----------|
| US-EP017-003 | As a Carrier, I want to see how many of my vehicles are AVAILABLE vs ASSIGNED on the dashboard so that I know my fleet utilisation at a glance. | Must |
| US-EP017-004 | As a Carrier, I want to see my overall warehouse fill level on the dashboard so that I am alerted before capacity is exceeded. | Should |

---

## Fleet Board

| ID | Story | Priority |
|----|-------|----------|
| US-EP017-005 | As a Carrier, I want to see all my vehicles with their current status and capacity so that I can assess availability for new assignments. | Must |
| US-EP017-006 | As a Carrier, I want to see a HAZMAT badge on certified vehicles and drivers so that I can quickly identify eligible resources for hazardous shipments (BR-003). | Must |
| US-EP017-007 | As a Carrier, I want to see a REFRIGERATED badge on refrigerated trucks so that I can identify cold-chain eligible vehicles (BR-008). | Must |
| US-EP017-008 | As a Carrier, I want to filter the vehicle list by status so that I can focus on AVAILABLE vehicles only. | Should |
| US-EP017-009 | As a Carrier, I want to see each driver's daily driving hours as a progress bar so that I can avoid assigning drivers close to the 9-hour limit (BR-005). | Must |
| US-EP017-010 | As a Carrier, I want the driver's progress bar to turn red when they have driven ≥ 8 hours so that I am warned before the BR-005 limit is reached. | Must |
| US-EP017-011 | As a Carrier, I want to see the driver status (AVAILABLE / DRIVING / RESTING / SUSPENDED) so that I know who is currently active. | Must |
| US-EP017-012 | As a Carrier, I want to click a driver to see their detail page with full hours history so that I can plan their schedule. | Should |

---

## Warehouse Dashboard

| ID | Story | Priority |
|----|-------|----------|
| US-EP017-013 | As a Carrier, I want to see capacity gauges for all my warehouses so that I can monitor storage levels at a glance. | Must |
| US-EP017-014 | As a Carrier, I want the capacity gauge to turn red when fill ≥ 90% so that I am warned before a BR-006 rejection occurs. | Must |
| US-EP017-015 | As a Carrier, I want to drill into a warehouse to see the per-SKU inventory breakdown so that I know exactly what is stored. | Should |
| US-EP017-016 | As a Carrier, I want SKUs expiring within 7 days highlighted in amber so that I can arrange removal before they expire. | Should |
| US-EP017-017 | As a Carrier, I want the warehouse gauges to refresh every 30 seconds so that capacity changes from inbound deliveries appear quickly. | Should |
