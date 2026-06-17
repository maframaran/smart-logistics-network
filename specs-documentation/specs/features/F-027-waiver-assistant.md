# Feature: F-027 — SLA Penalty Waiver Assistant

**Epic:** EP-018 — RAG Intelligence
**Phase:** 4

## Goal

When a Finance user or Shipper disputes an SLA penalty invoice, the system retrieves precedents from similar past late shipments and provides an LLM recommendation (WAIVE / UPHOLD / ESCALATE) with reasoning, standardising decisions and reducing manual review time.

## Actors

- **Finance User** — reviews disputed invoices and acts on the recommendation
- **Shipper** — views the waiver status on their billing page

## Preconditions

- The invoice exists and has status PENDING or OVERDUE
- At least some historical invoices have been indexed
- `rag-service` is running

## Workflow

1. `billing.invoice-generated` event → `RagKafkaConsumer` indexes into `rag.invoice_embeddings`
2. Finance user opens `/billing/{id}/waiver`, enters a dispute reason
3. UI POSTs to BFF `/api/rag/invoices/{id}/waiver` with `{reason}`
4. `rag-service` fetches invoice + associated shipment metadata → builds embedding → ANN search top-5 precedents
5. Claude Sonnet prompt with precedents + company policy → JSON `{recommendation, confidence, reasoning, precedents[]}`
6. UI renders recommendation card with WAIVE/UPHOLD/ESCALATE badge, confidence %, and precedent summaries

## Business Rules

- Embedding text: `"Invoice SLA:{slaType} daysLate:{n} penalty:{amount}BRL origin:{city} destination:{city} reason:{reason}"`
- Recommendation thresholds: confidence < 0.6 → always ESCALATE regardless of LLM output
- Policy injected into prompt: force majeure (weather, mechanical failure, infrastructure outage) → lean WAIVE; shipper error → lean UPHOLD
- Finance user can override; override is logged for future training data

## Edge Cases

- Invoice not found → 404 ProblemDetail
- No similar precedents → recommendation = ESCALATE with `lowConfidence: true`
- Reason field empty → treated as "no reason provided" in embedding

## Acceptance Criteria

```gherkin
Scenario: Waiver recommended for weather delay
  Given a PRIORITY invoice with 2 days late and penalty 300 BRL
  And 5 similar late invoices have been indexed with weather-related waivers granted
  When Finance POSTs /api/v1/rag/invoices/{id}/waiver with reason "weather delay on BR-116"
  Then the recommendation is WAIVE
  And confidence is greater than 0.6
  And at least 2 precedents are listed

Scenario: Low confidence escalation
  Given no similar precedents exist
  When Finance POSTs a waiver request
  Then the recommendation is ESCALATE
  And lowConfidence is true

Scenario: Waiver page visible to Finance
  Given a PRIORITY invoice with status OVERDUE
  When Finance navigates to /billing/{id}/waiver
  Then a form to enter a dispute reason is visible
  And after submission a recommendation card appears with WAIVE/UPHOLD/ESCALATE badge
```

## Telemetry

- `rag.waiver.requests.total` — counter by recommendation outcome
- `rag.waiver.override.count` — counter; Finance overrides recommendation
- `rag.waiver.llm.latency` — histogram
