package com.logistics.rag.application.usecases;

import com.fasterxml.jackson.databind.JsonNode;
import com.logistics.rag.domain.model.InventoryRecommendation;
import com.logistics.rag.domain.ports.out.EmbeddingPort;
import com.logistics.rag.domain.ports.out.LlmPort;
import com.logistics.rag.domain.ports.out.VectorStorePort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class InventoryAdvisorService {

    private static final double REBALANCE_THRESHOLD = 80.0; // % utilisation
    private static final double TARGET_FILL_MAX = 70.0;     // % after rebalancing

    private static final String TOOL_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "recommendations": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "sku":                { "type": "string" },
                      "suggestedQtyToMove": { "type": "integer" },
                      "targetWarehouseId":  { "type": "string" },
                      "fillPctAfter":       { "type": "number" },
                      "reasoning":          { "type": "string" }
                    },
                    "required": ["sku","suggestedQtyToMove","targetWarehouseId","fillPctAfter","reasoning"]
                  }
                },
                "reason": { "type": ["string","null"] }
              },
              "required": ["recommendations"]
            }
            """;

    private final EmbeddingPort embedding;
    private final LlmPort llm;
    private final VectorStorePort vectorStore;

    public InventoryAdvisorService(EmbeddingPort embedding, LlmPort llm, VectorStorePort vectorStore) {
        this.embedding = embedding;
        this.llm = llm;
        this.vectorStore = vectorStore;
    }

    public void index(String warehouseId, Map<String, Object> payload) {
        String text = "warehouse utilisation=" + payload.getOrDefault("utilisationPct", 0)
                + " location=" + payload.getOrDefault("location", "");
        float[] vec = embedding.embed(text);
        Map<String, Object> meta = Map.of(
                "warehouseName", orEmpty(payload.get("warehouseName")),
                "location", orEmpty(payload.get("location")),
                "maxWeightKg", toDouble(payload.get("maxWeightKg")),
                "maxVolumeM3", toDouble(payload.get("maxVolumeM3")),
                "currentWeightKg", toDouble(payload.get("currentWeightKg")),
                "currentVolumeM3", toDouble(payload.get("currentVolumeM3")),
                "utilisationPct", toDouble(payload.get("utilisationPct"))
        );
        vectorStore.upsertInventory(warehouseId, vec, meta);
    }

    public InventoryRecommendation rebalance(String warehouseId) {
        List<Map<String, Object>> all = vectorStore.findAllInventory();

        Map<String, Object> source = all.stream()
                .filter(w -> warehouseId.equals(str(w.get("warehouse_id"))))
                .findFirst().orElse(null);

        if (source == null) {
            return new InventoryRecommendation(List.of(), "WAREHOUSE_NOT_FOUND");
        }

        double utilisation = toDouble(source.get("utilisation_pct"));
        if (utilisation < REBALANCE_THRESHOLD) {
            return new InventoryRecommendation(List.of(), null);
        }

        List<Map<String, Object>> targets = all.stream()
                .filter(w -> !warehouseId.equals(str(w.get("warehouse_id"))))
                .filter(w -> toDouble(w.get("utilisation_pct")) < TARGET_FILL_MAX)
                .toList();

        if (targets.isEmpty()) {
            return new InventoryRecommendation(List.of(), "NO_SUITABLE_TARGET");
        }

        StringBuilder context = new StringBuilder("Source warehouse: ")
                .append(source.get("warehouse_id"))
                .append(", utilisation=").append(utilisation).append("%")
                .append("\nAvailable target warehouses:\n");
        for (Map<String, Object> t : targets) {
            context.append("- id=").append(t.get("warehouse_id"))
                   .append(", utilisation=").append(t.get("utilisation_pct")).append("%")
                   .append(", maxWeightKg=").append(t.get("max_weight_kg")).append("\n");
        }
        context.append("Recommend moving specific SKUs from source to reduce its utilisation below ")
               .append(TARGET_FILL_MAX).append("%.");

        JsonNode result = llm.complete(
                "You are a warehouse operations advisor. Recommend inventory rebalancing.",
                context.toString(), "rebalance_recommendation",
                "Return rebalancing recommendations.",
                TOOL_SCHEMA);

        List<InventoryRecommendation.RebalanceAction> actions = new ArrayList<>();
        JsonNode recs = result.path("recommendations");
        if (recs.isArray()) {
            for (JsonNode rec : recs) {
                actions.add(new InventoryRecommendation.RebalanceAction(
                        rec.path("sku").asText(),
                        rec.path("suggestedQtyToMove").asInt(),
                        rec.path("targetWarehouseId").asText(),
                        rec.path("fillPctAfter").asDouble(),
                        rec.path("reasoning").asText()));
            }
        }
        String reason = result.path("reason").isNull() ? null : result.path("reason").asText(null);
        return new InventoryRecommendation(actions, reason);
    }

    private double toDouble(Object v) { return v instanceof Number n ? n.doubleValue() : 0.0; }
    private String str(Object v) { return v != null ? v.toString() : ""; }
    private String orEmpty(Object v) { return v != null ? v.toString() : ""; }
}
