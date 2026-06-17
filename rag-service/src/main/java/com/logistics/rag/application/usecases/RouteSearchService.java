package com.logistics.rag.application.usecases;

import com.logistics.rag.domain.model.RouteSearchResult;
import com.logistics.rag.domain.ports.out.EmbeddingPort;
import com.logistics.rag.domain.ports.out.VectorStorePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RouteSearchService {

    private final EmbeddingPort embedding;
    private final VectorStorePort vectorStore;
    private final int topK;

    public RouteSearchService(EmbeddingPort embedding, VectorStorePort vectorStore,
                               @Value("${rag.vector.top-k:5}") int topK) {
        this.embedding = embedding;
        this.vectorStore = vectorStore;
        this.topK = topK;
    }

    public void index(String routeId, Map<String, Object> payload) {
        String text = buildRouteText(payload);
        float[] vec = embedding.embed(text);
        double fuelCost = toDouble(payload.get("fuelCostBrl"));
        double tollsCost = toDouble(payload.get("tollsCostBrl"));
        Map<String, Object> meta = Map.of(
                "shipmentId", orEmpty(payload.get("shipmentId")),
                "originCity", orEmpty(payload.get("originCity")),
                "destinationCity", orEmpty(payload.get("destinationCity")),
                "vehicleType", orEmpty(payload.get("vehicleType")),
                "slaType", "STANDARD",
                "distanceKm", toDouble(payload.get("totalDistanceKm")),
                "durationMinutes", toLong(payload.get("totalDurationMinutes")),
                "fuelCostBrl", fuelCost,
                "tollCostBrl", tollsCost,
                "totalCostBrl", fuelCost + tollsCost
        );
        vectorStore.upsertRoute(routeId, vec, meta);
    }

    public RouteSearchResult search(String originCity, String destinationCity,
                                     String vehicleType, String slaType) {
        String queryText = "route from " + originCity + " to " + destinationCity
                + " vehicle " + vehicleType + " sla " + slaType;
        float[] vec = embedding.embed(queryText);
        List<Map<String, Object>> rows = vectorStore.findSimilarRoutes(vec, topK);

        if (rows.isEmpty()) {
            return new RouteSearchResult(0, 0, List.of(), true);
        }

        List<RouteSearchResult.RouteComparable> comparables = new ArrayList<>();
        double totalCost = 0;
        long totalDuration = 0;
        for (Map<String, Object> row : rows) {
            double cost = toDouble(row.get("total_cost_brl"));
            long dur = toLong(row.get("duration_minutes"));
            double sim = toDouble(row.get("similarity"));
            totalCost += cost;
            totalDuration += dur;
            comparables.add(new RouteSearchResult.RouteComparable(
                    str(row.get("route_id")), str(row.get("shipment_id")), cost, dur, sim));
        }
        double avgCost = totalCost / rows.size();
        long avgDuration = totalDuration / rows.size();
        boolean lowConfidence = rows.size() < 3 || toDouble(rows.get(0).get("similarity")) < 0.5;
        return new RouteSearchResult(avgCost, avgDuration, comparables, lowConfidence);
    }

    private String buildRouteText(Map<String, Object> p) {
        return "route origin=" + p.getOrDefault("originCity", "") +
               " destination=" + p.getOrDefault("destinationCity", "") +
               " vehicle=" + p.getOrDefault("vehicleType", "") +
               " distance=" + p.getOrDefault("totalDistanceKm", 0) +
               " duration=" + p.getOrDefault("totalDurationMinutes", 0);
    }

    private double toDouble(Object v) { return v instanceof Number n ? n.doubleValue() : 0.0; }
    private long toLong(Object v) { return v instanceof Number n ? n.longValue() : 0L; }
    private String str(Object v) { return v != null ? v.toString() : ""; }
    private String orEmpty(Object v) { return v != null ? v.toString() : ""; }
}
