package com.logistics.billing.domain.events;

import com.logistics.billing.domain.model.Money;
import com.logistics.common.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InvoicePaid(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        String shipmentId,
        String carrierId,
        Money amountPaid
) implements DomainEvent {

    public static InvoicePaid of(String invoiceId, String shipmentId, String carrierId, Money amount) {
        return new InvoicePaid(UUID.randomUUID(), Instant.now(), invoiceId, shipmentId, carrierId, amount);
    }
}
