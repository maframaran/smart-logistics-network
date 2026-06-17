# Epic: EP-018 — RAG Intelligence

**Phase:** 4
**Domain:** Cross-cutting / AI

## Problem

The platform accumulates rich operational history — routes, invoices, shipment patterns, warehouse events — but this data is only used for transactions. Finance teams make waiver decisions from memory. Pricing is static. ETAs are estimated without historical context. Inventory rebalancing is reactive and manual.

RAG (Retrieval-Augmented Generation) closes this gap: embed historical records into a vector store, retrieve the most relevant ones at decision time, and pass them as context to a language model that produces a structured recommendation.

## Success Metrics

- Route similarity P99 latency < 200 ms (excluding LLM call)
- Recommendation acceptance rate > 70% (Finance accepts WAIVE/UPHOLD without override)
- Dynamic pricing adoption rate > 60% of new invoices
- Inventory rebalancing reduces warehouse overflow events by 30%
- Demand forecast 7-day MAPE < 15%

## Use Cases

| ID | Name | Actor | Trigger | LLM Output |
|----|------|-------|---------|------------|
| F-026 | Route Similarity Search | Routing Engine / Shipper | ShipmentCreated event | `{estimatedCostBrl, estimatedDurationMinutes, comparables[]}` |
| F-027 | SLA Waiver Assistant | Finance / Shipper | Disputed invoice | `{recommendation, confidence, reasoning, precedents[]}` |
| F-028 | Dynamic Pricing Advisor | Billing Service | Before Invoice.create() | `{suggestedPriceBrl, confidencePct, lowerBound, upperBound}` |
| F-029 | Inventory Rebalancing Advisor | Carrier / Warehouse Operator | High fill % alert | `{recommendations[{sku, qty, targetWarehouse, reasoning}]}` |
| F-030 | Demand Forecast Context | Carrier / Platform | On-demand | `{expectedShipments, confidenceInterval, seasonalFactor}` |

## Shared Infrastructure

- Vector store: pgvector extension on existing PostgreSQL (`rag` schema)
- Embedding model: `claude-haiku-4-5-20251001` (structured text → float[1536])
- Completion model: `claude-sonnet-4-6` (structured JSON via tool_use)
- New service: `rag-service` (port 8088, Maven module, hexagonal architecture)

## Kafka Topics Consumed

| Topic | Use cases fed |
|-------|--------------|
| `routing.route-calculated` | F-026 |
| `billing.invoice-generated` | F-027, F-028 |
| `warehouse.capacity-updated` | F-029 |
| `shipment.created` | F-030 |

## Dependencies

- [ADR-024](../../adrs/ADR-024-rag-pgvector.md)
- [EP-007](EP-007-event-driven-architecture.md) — Kafka events feed the indexing pipeline
- [EP-011](EP-011-dynamic-pricing.md) — F-028 is the RAG component of dynamic pricing
- [EP-010](EP-010-demand-forecasting.md) — F-030 is the retrieval layer for demand forecasting
