package com.logistics.rag.application.usecases;

import com.fasterxml.jackson.databind.JsonNode;
import com.logistics.rag.domain.model.PriceRecommendation;
import com.logistics.rag.domain.ports.out.EmbeddingPort;
import com.logistics.rag.domain.ports.out.LlmPort;
import com.logistics.rag.domain.ports.out.VectorStorePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PricingAdvisorService {

    // Static base rates (BRL) by SLA tier — actual price may vary
    private static final Map<String, Double> STATIC_RATES = Map.of(
            "STANDARD", 250.0,
            "PRIORITY", 500.0,
            "EXPRESS",  800.0
    );

    private static final String TOOL_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "suggestedPriceBrl": { "type": "number" },
                "lowerBound": { "type": "number" },
                "upperBound": { "type": "number" },
                "confidencePct": { "type": "number" }
              },
              "required": ["suggestedPriceBrl","lowerBound","upperBound","confidencePct"]
            }
            """;

    private final EmbeddingPort embedding;
    private final LlmPort llm;
    private final VectorStorePort vectorStore;
    private final int topK;
    private final double capMultiplier;

    public PricingAdvisorService(EmbeddingPort embedding, LlmPort llm, VectorStorePort vectorStore,
                                  @Value("${rag.vector.top-k:5}") int topK,
                                  @Value("${rag.pricing.cap-multiplier:1.5}") double capMultiplier) {
        this.embedding = embedding;
        this.llm = llm;
        this.vectorStore = vectorStore;
        this.topK = topK;
        this.capMultiplier = capMultiplier;
    }

    public void index(String invoiceId, Map<String, Object> payload) {
        // Shared index with WaiverAssistantService — upsert handled there; pricing uses same rows
    }

    public PriceRecommendation recommend(String originCity, String destinationCity,
                                          double weightKg, String slaType, int warehouseUtilisationPct) {
        String queryText = "shipment pricing from " + originCity + " to " + destinationCity
                + " weight=" + weightKg + " sla=" + slaType
                + " warehouse_utilisation=" + warehouseUtilisationPct;
        float[] vec = embedding.embed(queryText);
        List<Map<String, Object>> rows = vectorStore.findSimilarInvoices(vec, topK);
        double staticRate = STATIC_RATES.getOrDefault(slaType, 250.0);
        double cap = staticRate * capMultiplier;

        if (rows.isEmpty()) {
            return new PriceRecommendation(staticRate, staticRate * 0.8, cap, 30.0, 0);
        }

        StringBuilder context = new StringBuilder("Pricing request: origin=").append(originCity)
                .append(", destination=").append(destinationCity)
                .append(", weightKg=").append(weightKg)
                .append(", slaType=").append(slaType)
                .append(", warehouseUtilisationPct=").append(warehouseUtilisationPct)
                .append("\nStatic base rate: ").append(staticRate).append(" BRL")
                .append("\nPrice cap (1.5x): ").append(cap).append(" BRL")
                .append("\nHistorical paid invoices:\n");
        for (Map<String, Object> r : rows) {
            context.append("- totalAmountBrl=").append(r.get("total_amount_brl"))
                   .append(", sla=").append(r.get("sla_type"))
                   .append(", similarity=").append(r.get("similarity")).append("\n");
        }

        JsonNode result = llm.complete(
                "You are a logistics pricing analyst. Recommend a competitive price within the cap.",
                context.toString(), "pricing_recommendation",
                "Return a structured price recommendation.",
                TOOL_SCHEMA);

        double suggested = result.path("suggestedPriceBrl").asDouble(staticRate);
        double lower = result.path("lowerBound").asDouble(staticRate * 0.85);
        double upper = result.path("upperBound").asDouble(cap);
        double confidence = result.path("confidencePct").asDouble(50.0);

        // Enforce cap — BR-028
        suggested = Math.min(suggested, cap);
        upper = Math.min(upper, cap);

        return new PriceRecommendation(suggested, lower, upper, confidence, rows.size());
    }
}
