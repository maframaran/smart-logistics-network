package com.logistics.rag.application.usecases;

import com.fasterxml.jackson.databind.JsonNode;
import com.logistics.rag.domain.model.InvoiceMetadata;
import com.logistics.rag.domain.model.InvoiceSearchRow;
import com.logistics.rag.domain.model.WaiverResult;
import com.logistics.rag.domain.ports.out.EmbeddingPort;
import com.logistics.rag.domain.ports.out.LlmPort;
import com.logistics.rag.domain.ports.out.VectorStorePort;
import com.logistics.rag.infrastructure.messaging.dto.InvoiceGeneratedPayload;
import com.logistics.rag.infrastructure.messaging.dto.MoneyPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class WaiverAssistantService {

    private static final String ESCALATE = "ESCALATE";

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

    public void index(String invoiceId, InvoiceGeneratedPayload payload) {
        String text = buildInvoiceText(payload);
        float[] vec = embedding.embed(text);
        InvoiceMetadata meta = new InvoiceMetadata(
                orEmpty(payload.shipmentId()),
                orEmpty(payload.shipperId()),
                orEmpty(payload.carrierId()),
                orEmpty(payload.originCity()),
                orEmpty(payload.destinationCity()),
                orEmpty(payload.slaType()),
                amountOf(payload.baseAmount()),
                payload.penaltyDaysLate(),
                amountOf(payload.penaltyAmount()),
                amountOf(payload.totalAmount()),
                "PENDING"
        );
        vectorStore.upsertInvoice(invoiceId, vec, meta);
    }

    public WaiverResult recommend(String invoiceId, String reason) {
        String queryText = "invoice waiver request reason=" + reason + " invoice=" + invoiceId;
        float[] vec = embedding.embed(queryText);
        List<InvoiceSearchRow> rows = vectorStore.findSimilarInvoices(vec, topK);

        if (rows.isEmpty()) {
            return new WaiverResult(ESCALATE, 0.0,
                    "No historical precedents found to support a recommendation.", List.of());
        }

        List<WaiverResult.WaiverPrecedent> precedents = new ArrayList<>();
        for (InvoiceSearchRow row : rows) {
            String decision = Objects.requireNonNullElse(row.waiverDecision(), "UNKNOWN");
            precedents.add(new WaiverResult.WaiverPrecedent(
                    row.invoiceId(), decision, reason,
                    row.penaltyDays(), row.penaltyAmountBrl()));
        }

        String context = buildPrecedentContext(rows, reason);
        JsonNode result = llm.complete(
                "You are a logistics finance assistant evaluating SLA penalty waiver requests. " +
                "Based on historical precedents, recommend WAIVE, UPHOLD, or ESCALATE.",
                context,
                "waiver_decision",
                "Return a structured waiver recommendation based on precedents.",
                TOOL_SCHEMA);

        String recommendation = result.path("recommendation").asText(ESCALATE);
        double confidence = result.path("confidence").asDouble(0.0);
        String reasoning = result.path("reasoning").asText("No reasoning provided.");

        // BR: confidence < threshold always escalates
        if (confidence < lowConfidenceThreshold) {
            recommendation = ESCALATE;
            reasoning = "Confidence below threshold (" + confidence + " < " + lowConfidenceThreshold + "). " + reasoning;
        }

        return new WaiverResult(recommendation, confidence, reasoning, precedents);
    }

    private String buildPrecedentContext(List<InvoiceSearchRow> rows, String reason) {
        StringBuilder sb = new StringBuilder("Waiver request reason: ").append(reason).append("\n\nPrecedents:\n");
        for (InvoiceSearchRow r : rows) {
            sb.append("- Invoice ").append(r.invoiceId())
              .append(", SLA=").append(r.slaType())
              .append(", penalty_days=").append(r.penaltyDays())
              .append(", decision=").append(Objects.requireNonNullElse(r.waiverDecision(), "UNKNOWN"))
              .append(", similarity=").append(r.similarity()).append("\n");
        }
        return sb.toString();
    }

    private String buildInvoiceText(InvoiceGeneratedPayload p) {
        return "invoice sla=" + orEmpty(p.slaType()) +
               " penalty_days=" + p.penaltyDaysLate() +
               " origin=" + orEmpty(p.originCity()) +
               " destination=" + orEmpty(p.destinationCity());
    }

    private double amountOf(MoneyPayload money) {
        return money != null && money.amount() != null ? money.amount().doubleValue() : 0.0;
    }

    private String orEmpty(Object v) { return v != null ? v.toString() : ""; }
}
