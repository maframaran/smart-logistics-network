# User Stories — EP-001 Shipment Management

## Shipper Stories

**US-001** As a Shipper, I want to create a shipment request with origin, destination, cargo weight/volume, and required delivery date so that the platform can arrange transportation for my goods.
_Priority: Must | Feature: F-001_

**US-002** As a Shipper, I want to receive a unique shipment ID immediately after submission so that I can track my shipment from the start.
_Priority: Must | Feature: F-001_

**US-003** As a Shipper, I want to specify whether my cargo requires cold chain or hazmat handling so that the platform assigns an appropriate vehicle and driver.
_Priority: Must | Feature: F-001_

**US-004** As a Shipper, I want to choose a delivery SLA tier (Standard, Priority, Express) so that I can balance cost against delivery urgency.
_Priority: Must | Feature: F-001_

**US-005** As a Shipper, I want to schedule a pickup window for my shipment so that I can coordinate with my warehouse operations team.
_Priority: Must | Feature: F-002_

**US-006** As a Shipper, I want to cancel a shipment that has not yet been picked up so that I am not charged for transportation I no longer need.
_Priority: Must | Feature: F-006_

**US-007** As a Shipper, I want to receive a clear reason when my cancellation request is rejected (e.g. shipment is already in transit) so that I understand my options.
_Priority: Should | Feature: F-006_

**US-008** As a Shipper, I want to track the real-time status of my shipment (SCHEDULED → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED) so that I can inform my customers.
_Priority: Must | Feature: F-005 (status visible after assignment)_

## Platform Administrator Stories

**US-009** As a Platform Administrator, I want to manually assign a specific vehicle and driver to a shipment so that I can handle exceptional cases where automated assignment fails.
_Priority: Must | Feature: F-005_

**US-010** As a Platform Administrator, I want to approve or reject cancellation requests for ASSIGNED shipments so that I can protect carriers from last-minute revenue loss.
_Priority: Must | Feature: F-006_

**US-011** As a Platform Administrator, I want to see all shipments pending assignment so that I can intervene quickly when automated assignment is blocked.
_Priority: Should | Feature: F-005_
