# Feature: F-026 — Route Similarity Search

**Epic:** EP-018 — RAG Intelligence
**Phase:** 4

## Goal

When a new shipment is created, retrieve the five most historically similar routes and use them as context for a cost and ETA estimate, improving accuracy before the Haversine engine runs.

## Actors

- **Routing Engine** — indexes `RouteCalculated` events; queries for similarity
- **Shipper** — sees the estimate in the shipment detail UI

## Preconditions

- At least one historical `RouteCalculated` event has been indexed into `rag.route_embeddings`
- `rag-service` is running and reachable

## Workflow

1. `routing.route-calculated` event arrives → `RagKafkaConsumer` builds embedding text from route metadata → calls Claude embedding API → upserts into `rag.route_embeddings`
2. Shipper opens `/shipments/{id}` → UI calls BFF `/api/rag/routes/similar`
3. `rag-service` embeds query params → pgvector ANN search → top-5 similar routes retrieved
4. Claude Sonnet prompt with 5 comparables → JSON response `{estimatedCostBrl, estimatedDurationMinutes, comparables[]}`
5. UI renders "Route Estimate" card alongside the actual route summary

## Business Rules

- Embedding text: `"Route from {originCity} to {destinationCity} via {vehicleType} SLA:{slaType} dist:{distanceKm}km duration:{durationMin}min fuel:{fuelBrl}BRL tolls:{tollsBrl}BRL"`
- If fewer than 3 comparables found, return response with `lowConfidence: true`
- If rag-service unavailable, UI omits the estimate card (graceful degradation)

## Edge Cases

- No historical routes yet → return `{comparables: [], lowConfidence: true}`
- Origin and destination identical to query → rank by SLA tier and vehicle type similarity
- rag-service timeout (> 3s) → UI shows "Estimate unavailable"

## Acceptance Criteria

```gherkin
Scenario: Similar routes exist
  Given 10 historical routes have been indexed
  And a new shipment from "São Paulo" to "Curitiba" with vehicle type TRUCK and SLA STANDARD
  When I call GET /api/v1/rag/routes/similar
  Then the response contains a list of at least 3 comparables
  And estimatedCostBrl is greater than 0
  And estimatedDurationMinutes is greater than 0

Scenario: No routes indexed yet
  Given no routes have been indexed
  When I call GET /api/v1/rag/routes/similar
  Then the response contains comparables: []
  And lowConfidence is true

Scenario: Route Estimate card appears in UI
  Given the Shipper is authenticated
  And a shipment with a calculated route exists
  When the Shipper opens the shipment detail page
  Then a "Route Estimate" card is visible
  And it shows estimatedCostBrl and estimatedDurationMinutes
```

## Telemetry

- `rag.route.index.count` — counter; routes indexed total
- `rag.route.search.latency` — histogram; ANN search latency (ms)
- `rag.route.llm.latency` — histogram; Claude completion latency (ms)
- `rag.route.low_confidence.count` — counter; queries returning lowConfidence
