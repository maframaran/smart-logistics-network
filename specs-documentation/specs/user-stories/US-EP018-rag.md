# User Stories — EP-018 RAG Intelligence

## Shipper

**US-EP018-001**
As a Shipper, I want to see a cost and ETA estimate on the shipment detail page based on similar historical routes, so that I can plan my logistics budget before the route is formally calculated.
- Acceptance: Route Estimate card visible on `/shipments/{id}` with `estimatedCostBrl` and `estimatedDurationMinutes`
- Feature: F-026

**US-EP018-002**
As a Shipper, I want to submit a dispute reason for an SLA penalty and receive an immediate WAIVE/UPHOLD recommendation backed by precedents, so that I don't have to wait days for a manual Finance review.
- Acceptance: Waiver page at `/billing/{id}/waiver`; response within 5 seconds; recommendation shown with reasoning
- Feature: F-027

---

## Carrier

**US-EP018-003**
As a Carrier, I want to see a ranked list of SKUs to dispatch to alternative warehouses when a warehouse is near capacity, so that I can act before an overflow event occurs.
- Acceptance: Rebalancing page at `/warehouse/{id}/rebalance`; each recommendation shows SKU, quantity, target warehouse, reasoning
- Feature: F-029

**US-EP018-004**
As a Carrier, I want to see a demand forecast for key shippers and routes on my dashboard, so that I can pre-position fleet and warehouse capacity for next month.
- Acceptance: "Demand Forecast" panel on `/dashboard` (CARRIER role); shows expected shipment range for next 30 days
- Feature: F-030

---

## Finance User

**US-EP018-005**
As a Finance User, I want the waiver recommendation to cite specific historical precedents with similar conditions, so that I can justify my decision in the audit trail.
- Acceptance: Recommendation response includes `precedents[]` with invoice IDs, daysLate, outcome, and route info
- Feature: F-027

**US-EP018-006**
As a Finance User, I want invoices to reflect market-calibrated prices rather than static tier rates, so that we maximise revenue while remaining competitive.
- Acceptance: `baseAmount` on new invoices uses `suggestedPriceBrl` from rag-service when confidence ≥ 60%
- Feature: F-028

---

## Platform Administrator

**US-EP018-007**
As a Platform Administrator, I want to monitor the RAG service's recommendation acceptance rate and fallback frequency, so that I can tune confidence thresholds and retrain embeddings when accuracy degrades.
- Acceptance: Metrics `rag.waiver.override.count`, `rag.pricing.fallback.count`, `rag.forecast.low_confidence.count` exposed on `/actuator/prometheus`
- Feature: F-026 – F-030
