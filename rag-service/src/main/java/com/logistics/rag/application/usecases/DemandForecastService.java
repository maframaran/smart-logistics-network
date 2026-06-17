package com.logistics.rag.application.usecases;

import com.fasterxml.jackson.databind.JsonNode;
import com.logistics.rag.domain.model.DemandForecast;
import com.logistics.rag.domain.ports.out.EmbeddingPort;
import com.logistics.rag.domain.ports.out.LlmPort;
import com.logistics.rag.domain.ports.out.VectorStorePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DemandForecastService {

    private static final String TOOL_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "expectedShipments": { "type": "integer" },
                "confidenceInterval": {
                  "type": "object",
                  "properties": { "low": { "type": "integer" }, "high": { "type": "integer" } },
                  "required": ["low","high"]
                }
              },
              "required": ["expectedShipments","confidenceInterval"]
            }
            """;

    private final EmbeddingPort embedding;
    private final LlmPort llm;
    private final VectorStorePort vectorStore;
    private final int topK;

    public DemandForecastService(EmbeddingPort embedding, LlmPort llm, VectorStorePort vectorStore,
                                  @Value("${rag.vector.top-k:5}") int topK) {
        this.embedding = embedding;
        this.llm = llm;
        this.vectorStore = vectorStore;
        this.topK = topK;
    }

    public void index(String shipmentId, Map<String, Object> payload) {
        String originCity = extractCity(payload, "origin");
        String destCity = extractCity(payload, "destination");
        String monthKey = monthKeyFromPayload(payload);
        String text = "shipment shipper=" + payload.getOrDefault("shipperId", "")
                + " from=" + originCity + " to=" + destCity + " sla=" + payload.getOrDefault("slaType", "");
        float[] vec = embedding.embed(text);

        Object cargo = payload.get("cargo");
        double weightKg = 0;
        double volumeM3 = 0;
        boolean hazmat = false;
        boolean cold = false;
        if (cargo instanceof Map<?,?> cm) {
            weightKg = toDouble(cm.get("weightKg"));
            volumeM3 = toDouble(cm.get("volumeM3"));
            hazmat = Boolean.TRUE.equals(cm.get("requiresHazmat"));
            cold = Boolean.TRUE.equals(cm.get("requiresColdChain"));
        }

        Map<String, Object> meta = Map.of(
                "shipperId", orEmpty(payload.get("shipperId")),
                "originCity", originCity,
                "destinationCity", destCity,
                "slaType", orEmpty(payload.get("slaType")),
                "weightKg", weightKg,
                "volumeM3", volumeM3,
                "requiresHazmat", hazmat,
                "requiresColdChain", cold,
                "monthKey", monthKey
        );
        vectorStore.upsertShipment(shipmentId, vec, meta);
    }

    public DemandForecast forecast(String shipperId, String originCity, String destinationCity, String targetMonth) {
        String queryText = "shipment demand shipper=" + shipperId
                + " from=" + originCity + " to=" + destinationCity + " month=" + targetMonth;
        float[] vec = embedding.embed(queryText);
        List<Map<String, Object>> rows = vectorStore.findSimilarShipments(vec, topK);

        if (rows.isEmpty()) {
            return new DemandForecast(0, new DemandForecast.ConfidenceInterval(0, 0), List.of(), false);
        }

        // Group by month key and count
        Map<String, Long> byMonth = new java.util.LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            String mk = str(r.get("month_key"));
            byMonth.merge(mk, 1L, Long::sum);
        }

        boolean calendarBonus = byMonth.containsKey(targetMonth.substring(5)); // same MM
        List<DemandForecast.ForecastComparable> comparables = new ArrayList<>();
        for (Map.Entry<String, Long> e : byMonth.entrySet()) {
            comparables.add(new DemandForecast.ForecastComparable(e.getKey(), e.getValue().intValue()));
        }

        String context = "Forecast request: shipperId=" + shipperId
                + ", originCity=" + originCity + ", destinationCity=" + destinationCity
                + ", targetMonth=" + targetMonth
                + "\nHistorical monthly counts:\n" + byMonth;

        JsonNode result = llm.complete(
                "You are a logistics demand forecasting analyst.",
                context, "demand_forecast",
                "Return a demand forecast with confidence interval.",
                TOOL_SCHEMA);

        int expected = result.path("expectedShipments").asInt(rows.size());
        int low = result.path("confidenceInterval").path("low").asInt((int)(expected * 0.8));
        int high = result.path("confidenceInterval").path("high").asInt((int)(expected * 1.2));

        // +20% calendar bonus (same month)
        if (calendarBonus) {
            expected = (int)(expected * 1.2);
            low = (int)(low * 1.2);
            high = (int)(high * 1.2);
        }

        return new DemandForecast(expected, new DemandForecast.ConfidenceInterval(low, high), comparables, calendarBonus);
    }

    private String extractCity(Map<String, Object> payload, String key) {
        Object v = payload.get(key);
        if (v instanceof Map<?,?> m) return orEmpty(m.get("city"));
        return "";
    }

    private String monthKeyFromPayload(Map<String, Object> payload) {
        // Use indexed_at time as proxy for shipment month
        return YearMonth.now(ZoneOffset.UTC).toString();
    }

    private double toDouble(Object v) { return v instanceof Number n ? n.doubleValue() : 0.0; }
    private String str(Object v) { return v != null ? v.toString() : ""; }
    private String orEmpty(Object v) { return v != null ? v.toString() : ""; }
}
