# Feature: F-028 — Dynamic Pricing Advisor

**Epic:** EP-018 — RAG Intelligence
**Phase:** 4

## Goal

Before an invoice is generated, retrieve the 10 most similar paid historical shipments and use them to recommend a market-calibrated price (base amount) with a confidence band, replacing the static SLA-tier rate.

## Actors

- **Billing Service** — calls rag-service before `Invoice.create()`
- **Finance User** — reviews pricing recommendations in audit log
- **Carrier** — price affects payment amount

## Preconditions

- Historical PAID invoices have been indexed into `rag.invoice_embeddings`
- `rag-service` is running; billing-service falls back to static rate if unavailable

## Workflow

1. `billing.invoice-generated` events index into `rag.invoice_embeddings` (shared with F-027)
2. Before generating a new invoice, `GenerateInvoiceService` POSTs to `rag-service` `/api/v1/rag/pricing/recommend`
3. `rag-service` filters `rag.invoice_embeddings` to `status = PAID` only (accepted prices), runs ANN search top-10
4. Claude Haiku prompt → JSON `{suggestedPriceBrl, confidencePct, lowerBound, upperBound, comparables[]}`
5. `GenerateInvoiceService` uses `suggestedPriceBrl` as `baseAmount` if `confidencePct >= 60`, otherwise falls back to static rate
6. Both the suggested price and the static fallback rate are stored for A/B analysis

## Business Rules

- Embedding text: `"Invoice SLA:{slaType} origin:{city} destination:{city} weight:{w}kg month:{MM} warehouseFill:{pct}%"`
- Only PAID invoices are used as comparables (excludes disputes and overdue records)
- Confidence threshold: < 60% → use static rate; ≥ 60% → use suggested price
- Maximum markup vs. static rate: 50% (hard cap — LLM output clamped server-side)
- Minimum price: never below static rate × 0.8

## Edge Cases

- rag-service timeout (> 2s) → billing-service uses static rate, logs `pricing.fallback` metric
- Fewer than 3 PAID comparables → `confidencePct` capped at 50%, triggers static fallback
- Negative suggested price → rejected, static rate used, alert raised

## Acceptance Criteria

```gherkin
Scenario: Pricing recommendation returned
  Given 10 paid STANDARD invoices indexed for São Paulo → Rio de Janeiro
  When billing-service requests pricing for a new STANDARD shipment on the same route
  Then the response contains suggestedPriceBrl greater than 0
  And confidencePct is between 0 and 100
  And lowerBound is less than suggestedPriceBrl
  And upperBound is greater than suggestedPriceBrl

Scenario: Fallback to static rate on low confidence
  Given fewer than 3 paid comparables exist
  When billing-service requests pricing
  Then confidencePct is at most 50
  And billing-service logs the pricing.fallback metric

Scenario: Price capped at 1.5× static rate
  Given LLM suggests a price 200% above static rate
  Then the returned suggestedPriceBrl is at most 1.5× the static SLA rate
```

## Telemetry

- `rag.pricing.recommendations.total` — counter
- `rag.pricing.fallback.count` — counter; static rate used
- `rag.pricing.confidence.histogram` — distribution of confidence scores
- `rag.pricing.markup.histogram` — distribution of suggested/static ratio
