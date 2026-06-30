package com.logistics.rag.domain.model;

/** Metadata indexed alongside an invoice's embedding in {@code rag.invoice_embeddings}. */
public record InvoiceMetadata(
        String shipmentId,
        String shipperId,
        String carrierId,
        String originCity,
        String destinationCity,
        String slaType,
        double baseAmountBrl,
        long penaltyDays,
        double penaltyAmountBrl,
        double totalAmountBrl,
        String status
) {
}
