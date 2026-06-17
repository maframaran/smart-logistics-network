# rag-service

## Overview

Provides AI-powered intelligence across the logistics platform using Retrieval-Augmented Generation (RAG). Embeds historical domain records into pgvector, retrieves similar records at query time, and uses Claude LLM to synthesize structured recommendations.

## Port

`8088`

## Technology

| Component | Choice |
|-----------|--------|
| Vector store | pgvector extension on shared PostgreSQL (`rag` schema) |
| Embedding model | `claude-haiku-4-5-20251001` (1536-dim) |
| LLM | `claude-sonnet-4-6` via tool_use structured JSON |
| Index type | IVFFlat, `lists=50`, cosine distance (`<=>`) |
| Event bus | Kafka (consumer only) |

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `ANTHROPIC_API_KEY` | **yes** | — | Claude API key. Never commit to source control. |
| `SPRING_DATASOURCE_URL` | yes | — | PostgreSQL JDBC URL (shared DB, `rag` schema) |
| `SPRING_DATASOURCE_USERNAME` | yes | — | DB username |
| `SPRING_DATASOURCE_PASSWORD` | yes | — | DB password |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | yes | `localhost:9092` | Kafka brokers |
| `SHIPMENT_SERVICE_URL` | no | `http://localhost:8081` | For cross-service queries |
| `FLEET_SERVICE_URL` | no | `http://localhost:8082` | — |
| `DRIVER_SERVICE_URL` | no | `http://localhost:8083` | — |
| `ROUTING_SERVICE_URL` | no | `http://localhost:8084` | — |
| `WAREHOUSE_SERVICE_URL` | no | `http://localhost:8085` | — |
| `BILLING_SERVICE_URL` | no | `http://localhost:8086` | — |

## Kafka Topics Consumed

| Topic | Event | Action |
|-------|-------|--------|
| `routing.route-calculated` | `RouteCalculated` | Embed and index route record |
| `billing.invoice-generated` | `InvoiceGenerated` | Embed and index invoice record (used by waiver + pricing) |
| `warehouse.capacity-updated` | `CapacityUpdated` | Embed and index inventory snapshot |
| `shipment.created` | `ShipmentCreated` | Embed and index shipment record for demand forecasting |

## REST Endpoints

### F-026 — Route Similarity

```
GET /api/v1/rag/routes/similar
  ?originCity=SaoPaulo
  &destinationCity=Curitiba
  &vehicleType=TRUCK
  &slaType=STANDARD
```

Response:
```json
{
  "estimatedCostBrl": 420.00,
  "estimatedDurationMinutes": 360,
  "comparables": [ { "routeId": "...", "costBrl": 410.00, "durationMinutes": 355, "similarity": 0.97 } ],
  "lowConfidence": false
}
```

### F-027 — Waiver Assistant

```
POST /api/v1/rag/invoices/{invoiceId}/waiver
Content-Type: application/json

{ "reason": "weather delay" }
```

Response:
```json
{
  "recommendation": "WAIVE",
  "confidence": 0.84,
  "reasoning": "3 of 5 precedents with weather-delay reason for PRIORITY SLA were waived...",
  "precedents": [ { "invoiceId": "...", "decision": "WAIVE", "reason": "..." } ]
}
```

`recommendation` is always one of `WAIVE`, `UPHOLD`, or `ESCALATE`. If `confidence < 0.6`, `recommendation` is always `ESCALATE`.

### F-028 — Dynamic Pricing

```
POST /api/v1/rag/pricing/recommend
Content-Type: application/json

{
  "originCity": "SaoPaulo",
  "destinationCity": "RioDeJaneiro",
  "weightKg": 500,
  "slaType": "STANDARD",
  "warehouseUtilizationPct": 78
}
```

Response:
```json
{
  "suggestedPriceBrl": 385.00,
  "lowerBound": 340.00,
  "upperBound": 430.00,
  "confidencePct": 72,
  "comparables": 8
}
```

Cap: `suggestedPriceBrl ≤ 1.5 × staticRate`. Only PAID invoices are used as comparables.

### F-029 — Inventory Advisor

```
GET /api/v1/rag/warehouses/{warehouseId}/rebalance
```

Response:
```json
{
  "recommendations": [
    {
      "sku": "SKU-042",
      "suggestedQtyToMove": 200,
      "targetWarehouseId": "...",
      "fillPctAfter": 65.0,
      "reasoning": "Target warehouse has sufficient capacity..."
    }
  ],
  "reason": null
}
```

When no rebalancing is possible (all targets at capacity): `recommendations` is empty and `reason` is `"NO_SUITABLE_TARGET"`.

### F-030 — Demand Forecast

```
GET /api/v1/rag/forecast
  ?shipperId=shipper-abc
  &originCity=SaoPaulo
  &destinationCity=RioDeJaneiro
  &targetMonth=2026-07
```

Response:
```json
{
  "expectedShipments": 22,
  "confidenceInterval": { "low": 18, "high": 27 },
  "comparables": [ { "month": "2026-06", "actual": 20 }, { "month": "2025-07", "actual": 19 } ],
  "calendarBonus": true
}
```

`calendarBonus: true` when `targetMonth` shares the same calendar month as a comparable in the historical window (+20% applied to base estimate).

## Health

`GET /actuator/health` — standard Spring Boot Actuator.

## ADR

[ADR-024](../../adrs/ADR-024-rag-pgvector.md) — pgvector + Claude API decision.

## Epic

[EP-018](../../specs/epics/EP-018-rag-intelligence.md) — RAG Intelligence epic.
