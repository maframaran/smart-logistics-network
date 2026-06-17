# Feature: F-030 — Demand Forecast Context

**Epic:** EP-018 — RAG Intelligence
**Phase:** 4

## Goal

Provide a structured demand forecast for a given shipper and route corridor by retrieving the most historically similar shipment-volume periods and passing them as few-shot context to Claude, enabling data-driven capacity planning for Carriers.

## Actors

- **Carrier** — views the forecast panel on the dashboard to plan fleet and warehouse allocation
- **Platform Administrator** — uses forecasts for capacity planning reports

## Preconditions

- Historical `ShipmentCreated` events have been indexed into `rag.shipment_embeddings`
- `rag-service` running

## Workflow

1. `shipment.created` event → `RagKafkaConsumer` upserts into `rag.shipment_embeddings`
2. Carrier views Dashboard → sees "Demand Forecast" panel (CARRIER role only)
3. Panel calls BFF `/api/rag/forecast?shipperId=&originCity=&destinationCity=&targetMonth=`
4. `rag-service` builds embedding for target period → ANN search top-10 similar past months
5. Claude Haiku prompt with 10 historical volume records + seasonal context → JSON `{expectedShipments, confidenceInterval: {low, high}, seasonalFactor, comparables[]}`
6. Dashboard panel renders a mini bar chart (Recharts) with forecast range

## Business Rules

- Embedding text: `"Shipper:{shipperId} from:{originCity} to:{destinationCity} SLA:{slaType} weight:{w}kg month:{YYYY-MM}"`
- Aggregation: group by (shipperId, originCity, destinationCity, YYYY-MM) before indexing — one vector per group, value = shipment count
- Time weight: records from the same calendar month in prior years score a 20% similarity bonus
- Minimum history for forecast: 3 comparable months; fewer → return `lowConfidence: true`

## Edge Cases

- New shipper with no history → return `{expectedShipments: 0, lowConfidence: true, comparables: []}`
- targetMonth in the past → valid (historical comparison); no special handling needed
- rag-service unavailable → Dashboard panel shows "Forecast unavailable" placeholder

## Acceptance Criteria

```gherkin
Scenario: Forecast returned for known shipper-route
  Given 12 months of shipment history for shipper "Acme" on São Paulo → Rio
  When Carrier GETs /api/v1/rag/forecast?shipperId=acme&originCity=SaoPaulo&destinationCity=Rio&targetMonth=2026-08
  Then expectedShipments is greater than 0
  And confidenceInterval.low is less than expectedShipments
  And confidenceInterval.high is greater than expectedShipments
  And at least 3 comparables are listed

Scenario: New shipper returns low confidence forecast
  Given no shipment history exists for shipper "NewCo"
  When Carrier GETs /api/v1/rag/forecast for shipper "NewCo"
  Then expectedShipments is 0
  And lowConfidence is true
  And comparables list is empty

Scenario: Forecast panel visible on Dashboard
  Given the Carrier is authenticated
  When the Carrier views the dashboard
  Then a "Demand Forecast" panel is visible
  And it shows a bar chart with forecast range for the next month
```

## Telemetry

- `rag.forecast.index.count` — counter; shipment records indexed
- `rag.forecast.requests.total` — counter
- `rag.forecast.low_confidence.count` — counter
- `rag.forecast.llm.latency` — histogram
