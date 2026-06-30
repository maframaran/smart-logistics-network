package com.logistics.rag.domain.model;

/**
 * A row returned by {@code findSimilarInvoices} ANN search, including cosine similarity.
 * waiverDecision is nullable — no caller currently writes it back after a recommendation.
 */
public record InvoiceSearchRow(
        String invoiceId,
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
        String status,
        String waiverDecision,
        double similarity
) {
}
