package com.logistics.rag.domain.model;

/** Metadata indexed alongside a shipment's embedding in {@code rag.shipment_embeddings}. */
public record ShipmentMetadata(
        String shipperId,
        String originCity,
        String destinationCity,
        String slaType,
        double weightKg,
        double volumeM3,
        boolean requiresHazmat,
        boolean requiresColdChain,
        String monthKey
) {
}
