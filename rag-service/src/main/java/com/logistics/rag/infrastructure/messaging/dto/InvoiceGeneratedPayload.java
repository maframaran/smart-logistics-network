package com.logistics.rag.infrastructure.messaging.dto;

/**
 * Mirrors billing-service's {@code InvoiceGenerated} domain event JSON shape.
 * originCity/destinationCity/slaType are not currently emitted by the producer event and
 * will deserialize as null — pre-existing gap, preserved as-is from the prior Map-based code.
 */
public record InvoiceGeneratedPayload(
        String shipmentId,
        String shipperId,
        String carrierId,
        String originCity,
        String destinationCity,
        String slaType,
        MoneyPayload baseAmount,
        long penaltyDaysLate,
        MoneyPayload penaltyAmount,
        MoneyPayload totalAmount
) {
}
