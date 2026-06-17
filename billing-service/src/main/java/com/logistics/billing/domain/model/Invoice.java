package com.logistics.billing.domain.model;

import com.logistics.common.domain.AggregateRoot;
import com.logistics.billing.domain.events.InvoiceGenerated;
import com.logistics.billing.domain.events.InvoicePaid;

import java.time.LocalDate;

public class Invoice extends AggregateRoot {

    private final InvoiceId id;
    private final String shipmentId;
    private final String shipperId;
    private final String carrierId;
    private final Money baseAmount;
    private final SlaPenalty slaPenalty;
    private final Money totalAmount;
    private final LocalDate dueDate;
    private InvoiceStatus status;

    private Invoice(InvoiceId id, String shipmentId, String shipperId, String carrierId,
                    Money baseAmount, SlaPenalty slaPenalty, Money totalAmount,
                    LocalDate dueDate, InvoiceStatus status) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.shipperId = shipperId;
        this.carrierId = carrierId;
        this.baseAmount = baseAmount;
        this.slaPenalty = slaPenalty;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
        this.status = status;
    }

    public static Invoice generate(String shipmentId, String shipperId, String carrierId,
                                   Money baseAmount, SlaPenalty slaPenalty, LocalDate dueDate) {
        if (shipmentId == null || shipmentId.isBlank()) throw new IllegalArgumentException("shipmentId must not be blank");
        if (dueDate == null) throw new IllegalArgumentException("dueDate must not be null");

        Money total = slaPenalty.applies()
                ? baseAmount.add(slaPenalty.penaltyAmount())
                : baseAmount;

        InvoiceId id = InvoiceId.generate();
        Invoice invoice = new Invoice(id, shipmentId, shipperId, carrierId,
                baseAmount, slaPenalty, total, dueDate, InvoiceStatus.PENDING);

        invoice.registerEvent(InvoiceGenerated.of(id.toString(), shipmentId, shipperId, carrierId,
                baseAmount, slaPenalty, total, dueDate));
        return invoice;
    }

    public static Invoice reconstitute(InvoiceId id, String shipmentId, String shipperId, String carrierId,
                                        Money baseAmount, SlaPenalty slaPenalty, Money totalAmount,
                                        LocalDate dueDate, InvoiceStatus status) {
        return new Invoice(id, shipmentId, shipperId, carrierId, baseAmount, slaPenalty, totalAmount, dueDate, status);
    }

    public void markPaid() {
        if (status != InvoiceStatus.PENDING && status != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("Cannot pay invoice in status: " + status);
        }
        this.status = InvoiceStatus.PAID;
        registerEvent(InvoicePaid.of(id.toString(), shipmentId, carrierId, totalAmount));
    }

    public void markOverdue() {
        if (status != InvoiceStatus.PENDING) throw new IllegalStateException("Only PENDING invoices can become OVERDUE");
        this.status = InvoiceStatus.OVERDUE;
    }

    public InvoiceId getId() { return id; }
    public String getShipmentId() { return shipmentId; }
    public String getShipperId() { return shipperId; }
    public String getCarrierId() { return carrierId; }
    public Money getBaseAmount() { return baseAmount; }
    public SlaPenalty getSlaPenalty() { return slaPenalty; }
    public Money getTotalAmount() { return totalAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public InvoiceStatus getStatus() { return status; }
}
