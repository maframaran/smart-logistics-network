package com.logistics.billing.domain.events;

import com.logistics.billing.domain.model.Money;
import com.logistics.billing.domain.model.SlaPenalty;
import com.logistics.common.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceGenerated(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        String shipmentId,
        String shipperId,
        String carrierId,
        Money baseAmount,
        long penaltyDaysLate,
        Money penaltyAmount,
        Money totalAmount,
        LocalDate dueDate
) implements DomainEvent {

    public static InvoiceGenerated of(String invoiceId, String shipmentId, String shipperId, String carrierId,
                                       Money baseAmount, SlaPenalty penalty, Money total, LocalDate dueDate) {
        return new InvoiceGenerated(UUID.randomUUID(), Instant.now(), invoiceId,
                shipmentId, shipperId, carrierId,
                baseAmount, penalty.daysLate(), penalty.penaltyAmount(), total, dueDate);
    }
}
