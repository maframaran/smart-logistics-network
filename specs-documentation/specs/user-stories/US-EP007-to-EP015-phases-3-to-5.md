# User Stories — EP-007 to EP-015 (Phases 3–5)

Stories at this level are intentionally high-level. They will be decomposed into detailed features and acceptance tests when the relevant phase begins implementation.

---

## EP-007 Event-Driven Architecture

**US-042** As a Platform Engineer, I want all cross-service communication to go through Kafka so that services are decoupled and independently deployable.
_Priority: Must | Epic: EP-007_

**US-043** As a Platform Engineer, I want dead-letter queues for all Kafka topics so that failed messages are not silently dropped.
_Priority: Must | Epic: EP-007_

**US-044** As a Platform Engineer, I want event schema versioning enforced at the broker level so that schema changes do not break consumers.
_Priority: Should | Epic: EP-007_

---

## EP-008 Microservices

**US-045** As a Platform Engineer, I want each service to expose an OpenAPI 3 contract so that consumers can generate type-safe clients.
_Priority: Must | Epic: EP-008_

**US-046** As an Operations Engineer, I want all services to expose `/actuator/health` and `/actuator/readiness` endpoints so that Kubernetes can manage pod lifecycle.
_Priority: Must | Epic: EP-008_

**US-047** As an Operations Engineer, I want distributed traces across services so that I can diagnose latency issues end-to-end.
_Priority: Should | Epic: EP-008_

---

## EP-009 CQRS

**US-048** As a Shipper, I want shipment tracking queries to return in under 50ms so that real-time status updates feel instant.
_Priority: Should | Epic: EP-009_

**US-049** As a Platform Engineer, I want read models built from Kafka events so that write-side performance is not impacted by reporting queries.
_Priority: Should | Epic: EP-009_

---

## EP-010 Demand Forecasting

**US-050** As a Platform Administrator, I want a 7-day shipment volume forecast so that I can pre-position vehicles and drivers before demand spikes.
_Priority: Should | Epic: EP-010_

---

## EP-011 Dynamic Pricing

**US-051** As a Shipper, I want to see the current dynamic price before confirming a shipment so that I can decide whether to proceed or wait.
_Priority: Must | Epic: EP-011_

**US-052** As a Platform Administrator, I want to set price surge caps so that prices never exceed a defined maximum multiple of the base rate.
_Priority: Must | Epic: EP-011_

---

## EP-012 Predictive Maintenance

**US-053** As a Carrier, I want to receive a maintenance alert 48 hours before a predicted vehicle failure so that I can schedule maintenance without disrupting active shipments.
_Priority: Should | Epic: EP-012_

---

## EP-013 Multi-Tenant SaaS

**US-054** As a New Logistics Company, I want to onboard to the platform in under one business day so that I can start using it quickly.
_Priority: Must | Epic: EP-013_

**US-055** As a Tenant Administrator, I want complete data isolation from other tenants so that my business data is never visible to competitors.
_Priority: Must | Epic: EP-013_

---

## EP-014 Global Network

**US-056** As a Shipper, I want to create cross-border shipments with automatic customs documentation so that I can expand internationally without manual paperwork.
_Priority: Should | Epic: EP-014_

---

## EP-015 Carrier Marketplace

**US-057** As a Carrier, I want to browse available shipment loads and submit bids so that I can find new business opportunities on the platform.
_Priority: Must | Epic: EP-015_

**US-058** As a Shipper, I want to award a shipment to the best bid automatically based on price and carrier rating so that I get competitive rates without manual negotiation.
_Priority: Should | Epic: EP-015_
