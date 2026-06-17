# Feature: F-029 — Inventory Rebalancing Advisor

**Epic:** EP-018 — RAG Intelligence
**Phase:** 4

## Goal

When a warehouse fill percentage exceeds a threshold, or on-demand from a Carrier, retrieve semantically similar SKU-and-warehouse records from all warehouses and produce a ranked list of dispatch recommendations, reducing overflow events and improving utilisation.

## Actors

- **Carrier / Warehouse Operator** — requests rebalancing recommendations
- **Warehouse Service** — triggers indexing via Kafka events

## Preconditions

- At least two warehouses registered with inventory
- `rag-service` running; pgvector has indexed current inventory state

## Workflow

1. `warehouse.capacity-updated` event → `RagKafkaConsumer` upserts into `rag.inventory_embeddings` (per SKU per warehouse)
2. Carrier opens `/warehouse/{id}/rebalance`
3. UI GETs BFF `/api/rag/warehouses/{id}/rebalance`
4. `rag-service` fetches all SKUs for target warehouse → embeds each → ANN search across all warehouses to find underutilised alternatives nearby
5. Also queries `rag.shipment_embeddings` for upcoming outbound shipments from same region (next 7 days)
6. Claude Sonnet prompt with capacity data + shipment forecast → JSON `{recommendations: [{sku, suggestedQtyToMove, targetWarehouseId, targetWarehouseName, fillPctAfter, reasoning}]}`

## Business Rules

- Embedding text: `"Warehouse:{name} location:{loc} SKU:{sku} qty:{n} fill:{pct}% weight:{w}kg volume:{v}m3"`
- Only recommend dispatching to warehouses with fill < 70% after receiving
- Prioritise high-velocity SKUs (those appearing in recent outbound `shipment.created` events for the destination region)
- Never recommend dispatching quantity that would bring source warehouse below 20% fill

## Edge Cases

- Only one warehouse registered → return `{recommendations: [], reason: "no alternative warehouse"}`
- All other warehouses also near capacity → recommend ESCALATE to operations
- Target warehouse already below 70% → return `{recommendations: [], reason: "rebalancing not needed"}`

## Acceptance Criteria

```gherkin
Scenario: Recommendations returned for overcrowded warehouse
  Given warehouse "SP-Central" is at 88% capacity
  And warehouse "SP-South" is at 45% capacity with space for rebalancing
  When Carrier GETs /api/v1/rag/warehouses/{spCentralId}/rebalance
  Then at least 1 recommendation is returned
  And each recommendation includes sku, suggestedQtyToMove, targetWarehouseId, reasoning
  And targetWarehouse fillPctAfter is below 70%

Scenario: No rebalancing needed
  Given warehouse "SP-Central" is at 55% capacity
  When Carrier GETs /api/v1/rag/warehouses/{spCentralId}/rebalance
  Then recommendations list is empty
  And reason is "rebalancing not needed"

Scenario: Rebalance page visible to Carrier
  Given the Carrier is authenticated
  When they navigate to /warehouse/{id}/rebalance
  Then a list of dispatch recommendations is shown
  And each row has SKU, quantity, target warehouse, and reasoning
```

## Telemetry

- `rag.inventory.index.count` — counter; SKU records indexed
- `rag.inventory.rebalance.requests` — counter
- `rag.inventory.recommendations.per_request` — histogram; recommendations count per call
