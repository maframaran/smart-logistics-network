package com.logistics.billing.infrastructure.messaging;

import com.logistics.billing.domain.events.InvoiceGenerated;
import com.logistics.billing.domain.events.InvoicePaid;
import com.logistics.common.domain.DomainEvent;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

@Component
class BillingEventAvroMapper {

    SpecificRecord toAvro(DomainEvent event) {
        return switch (event) {
            case InvoiceGenerated e -> toAvro(e);
            case InvoicePaid e      -> toAvro(e);
            default -> throw new IllegalArgumentException("Unknown event: " + event.getClass().getSimpleName());
        };
    }

    private com.logistics.billing.avro.InvoiceGenerated toAvro(InvoiceGenerated e) {
        return com.logistics.billing.avro.InvoiceGenerated.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setShipmentId(e.shipmentId())
                .setShipperId(e.shipperId())
                .setCarrierId(e.carrierId())
                .setBaseAmountBrl(e.baseAmount().amount().toPlainString())
                .setBaseCurrency(e.baseAmount().currency())
                .setPenaltyDaysLate(e.penaltyDaysLate())
                .setPenaltyAmountBrl(e.penaltyAmount().amount().toPlainString())
                .setPenaltyCurrency(e.penaltyAmount().currency())
                .setTotalAmountBrl(e.totalAmount().amount().toPlainString())
                .setTotalCurrency(e.totalAmount().currency())
                .setDueDate((int) e.dueDate().toEpochDay())
                .build();
    }

    private com.logistics.billing.avro.InvoicePaid toAvro(InvoicePaid e) {
        return com.logistics.billing.avro.InvoicePaid.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setShipmentId(e.shipmentId())
                .setCarrierId(e.carrierId())
                .setAmountBrl(e.amountPaid().amount().toPlainString())
                .setCurrency(e.amountPaid().currency())
                .build();
    }
}
