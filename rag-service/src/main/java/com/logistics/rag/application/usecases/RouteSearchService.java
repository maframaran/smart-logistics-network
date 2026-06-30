package com.logistics.rag.application.usecases;

import com.logistics.rag.domain.model.RouteMetadata;
import com.logistics.rag.domain.model.RouteSearchResult;
import com.logistics.rag.domain.model.RouteSearchRow;
import com.logistics.rag.domain.ports.out.EmbeddingPort;
import com.logistics.rag.domain.ports.out.VectorStorePort;
import com.logistics.rag.infrastructure.messaging.dto.RouteCalculatedPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public void index(String routeId, RouteCalculatedPayload payload) {
        String text = buildRouteText(payload);
        float[] vec = embedding.embed(text);
        double fuelCost = payload.fuelCostBrl();
        double tollsCost = payload.tollsCostBrl();
        RouteMetadata meta = new RouteMetadata(
                orEmpty(payload.shipmentId()),
                orEmpty(payload.originCity()),
                orEmpty(payload.destinationCity()),
                orEmpty(payload.vehicleType()),
                "STANDARD",
                payload.totalDistanceKm(),
                payload.totalDurationMinutes(),
                fuelCost,
                tollsCost,
                fuelCost + tollsCost
        );
        vectorStore.upsertRoute(routeId, vec, meta);
    }

    public RouteSearchResult search(String originCity, String destinationCity,
                                     String vehicleType, String slaType) {
        String queryText = "route from " + originCity + " to " + destinationCity
                + " vehicle " + vehicleType + " sla " + slaType;
        float[] vec = embedding.embed(queryText);
        List<RouteSearchRow> rows = vectorStore.findSimilarRoutes(vec, topK);

        if (rows.isEmpty()) {
            return new RouteSearchResult(0, 0, List.of(), true);
        }

        List<RouteSearchResult.RouteComparable> comparables = new ArrayList<>();
        double totalCost = 0;
        long totalDuration = 0;
        for (RouteSearchRow row : rows) {
            totalCost += row.totalCostBrl();
            totalDuration += row.durationMinutes();
            comparables.add(new RouteSearchResult.RouteComparable(
                    row.routeId(), row.shipmentId(), row.totalCostBrl(), row.durationMinutes(), row.similarity()));
        }
        double avgCost = totalCost / rows.size();
        long avgDuration = totalDuration / rows.size();
        boolean lowConfidence = rows.size() < 3 || rows.get(0).similarity() < 0.5;
        return new RouteSearchResult(avgCost, avgDuration, comparables, lowConfidence);
    }

    private String buildRouteText(RouteCalculatedPayload p) {
        return "route origin=" + orEmpty(p.originCity()) +
               " destination=" + orEmpty(p.destinationCity()) +
               " vehicle=" + orEmpty(p.vehicleType()) +
               " distance=" + p.totalDistanceKm() +
               " duration=" + p.totalDurationMinutes();
    }

    private String orEmpty(Object v) { return v != null ? v.toString() : ""; }
}
