package com.logistics.rag.application.usecases;

import com.fasterxml.jackson.databind.JsonNode;
import com.logistics.rag.domain.model.InventoryMetadata;
import com.logistics.rag.domain.model.InventoryRecommendation;
import com.logistics.rag.domain.model.InventoryRow;
import com.logistics.rag.domain.ports.out.EmbeddingPort;
import com.logistics.rag.domain.ports.out.LlmPort;
import com.logistics.rag.domain.ports.out.VectorStorePort;
import com.logistics.rag.infrastructure.messaging.dto.WarehouseCapacityUpdatedPayload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public void index(String warehouseId, WarehouseCapacityUpdatedPayload payload) {
        String text = "warehouse utilisation=" + payload.utilisationPct()
                + " location=" + orEmpty(payload.location());
        float[] vec = embedding.embed(text);
        InventoryMetadata meta = new InventoryMetadata(
                orEmpty(payload.warehouseName()),
                orEmpty(payload.location()),
                payload.maxWeightKg(),
                payload.maxVolumeM3(),
                payload.currentWeightKg(),
                payload.currentVolumeM3(),
                payload.utilisationPct()
        );
        vectorStore.upsertInventory(warehouseId, vec, meta);
    }

    public InventoryRecommendation rebalance(String warehouseId) {
        List<InventoryRow> all = vectorStore.findAllInventory();

        InventoryRow source = all.stream()
                .filter(w -> warehouseId.equals(w.warehouseId()))
                .findFirst().orElse(null);

        if (source == null) {
            return new InventoryRecommendation(List.of(), "WAREHOUSE_NOT_FOUND");
        }

        double utilisation = source.utilisationPct();
        if (utilisation < REBALANCE_THRESHOLD) {
            return new InventoryRecommendation(List.of(), null);
        }

        List<InventoryRow> targets = all.stream()
                .filter(w -> !warehouseId.equals(w.warehouseId()))
                .filter(w -> w.utilisationPct() < TARGET_FILL_MAX)
                .toList();

        if (targets.isEmpty()) {
            return new InventoryRecommendation(List.of(), "NO_SUITABLE_TARGET");
        }

        StringBuilder context = new StringBuilder("Source warehouse: ")
                .append(source.warehouseId())
                .append(", utilisation=").append(utilisation).append("%")
                .append("\nAvailable target warehouses:\n");
        for (InventoryRow t : targets) {
            context.append("- id=").append(t.warehouseId())
                   .append(", utilisation=").append(t.utilisationPct()).append("%")
                   .append(", maxWeightKg=").append(t.maxWeightKg()).append("\n");
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

    private String orEmpty(Object v) { return v != null ? v.toString() : ""; }
}
