package com.logistics.rag.application.usecases;

import com.fasterxml.jackson.databind.JsonNode;
import com.logistics.rag.domain.model.WaiverResult;
import com.logistics.rag.domain.ports.out.EmbeddingPort;
import com.logistics.rag.domain.ports.out.LlmPort;
import com.logistics.rag.domain.ports.out.VectorStorePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WaiverAssistantService {

    private static final String TOOL_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "recommendation": { "type": "string", "enum": ["WAIVE","UPHOLD","ESCALATE"] },
                "confidence": { "type": "number" },
                "reasoning": { "type": "string" }
              },
              "required": ["recommendation","confidence","reasoning"]
            }
            """;

    private final EmbeddingPort embedding;
    private final LlmPort llm;
    private final VectorStorePort vectorStore;
    private final int topK;
    private final double lowConfidenceThreshold;

    public WaiverAssistantService(EmbeddingPort embedding, LlmPort llm, VectorStorePort vectorStore,
                                   @Value("${rag.vector.top-k:5}") int topK,
                                   @Value("${rag.vector.low-confidence-threshold:0.6}") double threshold) {
        this.embedding = embedding;
        this.llm = llm;
        this.vectorStore = vectorStore;
        this.topK = topK;
        this.lowConfidenceThreshold = threshold;
    }

    public void index(String invoiceId, Map<String, Object> payload) {
        String text = buildInvoiceText(payload);
        float[] vec = embedding.embed(text);
        Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("shipmentId", orEmpty(payload.get("shipmentId")));
        meta.put("shipperId", orEmpty(payload.get("shipperId")));
        meta.put("carrierId", orEmpty(payload.get("carrierId")));
        meta.put("originCity", orEmpty(payload.get("originCity")));
        meta.put("destinationCity", orEmpty(payload.get("destinationCity")));
        meta.put("slaType", orEmpty(payload.get("slaType")));
        meta.put("baseAmountBrl", toDouble(nestedAmount(payload, "baseAmount", "amount")));
        meta.put("penaltyDays", toLong(payload.get("penaltyDaysLate")));
        meta.put("penaltyAmountBrl", toDouble(nestedAmount(payload, "penaltyAmount", "amount")));
        meta.put("totalAmountBrl", toDouble(nestedAmount(payload, "totalAmount", "amount")));
        meta.put("status", "PENDING");
        vectorStore.upsertInvoice(invoiceId, vec, meta);
    }

    public WaiverResult recommend(String invoiceId, String reason) {
        String queryText = "invoice waiver request reason=" + reason + " invoice=" + invoiceId;
        float[] vec = embedding.embed(queryText);
        List<Map<String, Object>> rows = vectorStore.findSimilarInvoices(vec, topK);

        if (rows.isEmpty()) {
            return new WaiverResult("ESCALATE", 0.0,
                    "No historical precedents found to support a recommendation.", List.of());
        }

        List<WaiverResult.WaiverPrecedent> precedents = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String decision = row.get("waiver_decision") != null ? row.get("waiver_decision").toString() : "UNKNOWN";
            precedents.add(new WaiverResult.WaiverPrecedent(
                    str(row.get("invoice_id")), decision, reason,
                    toLong(row.get("penalty_days")), toDouble(row.get("penalty_amount_brl"))));
        }

        String context = buildPrecedentContext(rows, reason);
        JsonNode result = llm.complete(
                "You are a logistics finance assistant evaluating SLA penalty waiver requests. " +
                "Based on historical precedents, recommend WAIVE, UPHOLD, or ESCALATE.",
                context,
                "waiver_decision",
                "Return a structured waiver recommendation based on precedents.",
                TOOL_SCHEMA);

        String recommendation = result.path("recommendation").asText("ESCALATE");
        double confidence = result.path("confidence").asDouble(0.0);
        String reasoning = result.path("reasoning").asText("No reasoning provided.");

        // BR: confidence < threshold always escalates
        if (confidence < lowConfidenceThreshold) {
            recommendation = "ESCALATE";
            reasoning = "Confidence below threshold (" + confidence + " < " + lowConfidenceThreshold + "). " + reasoning;
        }

        return new WaiverResult(recommendation, confidence, reasoning, precedents);
    }

    private String buildPrecedentContext(List<Map<String, Object>> rows, String reason) {
        StringBuilder sb = new StringBuilder("Waiver request reason: ").append(reason).append("\n\nPrecedents:\n");
        for (Map<String, Object> r : rows) {
            sb.append("- Invoice ").append(r.get("invoice_id"))
              .append(", SLA=").append(r.get("sla_type"))
              .append(", penalty_days=").append(r.get("penalty_days"))
              .append(", decision=").append(r.getOrDefault("waiver_decision", "UNKNOWN"))
              .append(", similarity=").append(r.get("similarity")).append("\n");
        }
        return sb.toString();
    }

    private String buildInvoiceText(Map<String, Object> p) {
        return "invoice sla=" + p.getOrDefault("slaType", "") +
               " penalty_days=" + p.getOrDefault("penaltyDaysLate", 0) +
               " origin=" + p.getOrDefault("originCity", "") +
               " destination=" + p.getOrDefault("destinationCity", "");
    }

    private Object nestedAmount(Map<String, Object> payload, String key, String field) {
        Object v = payload.get(key);
        if (v instanceof Map<?,?> m) return m.get(field);
        return v;
    }

    private double toDouble(Object v) { return v instanceof Number n ? n.doubleValue() : 0.0; }
    private long toLong(Object v) { return v instanceof Number n ? n.longValue() : 0L; }
    private String str(Object v) { return v != null ? v.toString() : ""; }
    private String orEmpty(Object v) { return v != null ? v.toString() : ""; }
}
