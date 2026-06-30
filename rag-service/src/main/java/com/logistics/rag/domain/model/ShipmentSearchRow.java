package com.logistics.rag.domain.model;

/** A row returned by {@code findSimilarShipments} ANN search, including cosine similarity. */
public record ShipmentSearchRow(
        String shipmentId,
        String shipperId,
        String originCity,
        String destinationCity,
        String slaType,
        double weightKg,
        double volumeM3,
        String monthKey,
        double similarity
) {
}
