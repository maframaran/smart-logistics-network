package com.logistics.billing.infrastructure.persistence;

import com.logistics.billing.domain.model.*;
import com.logistics.billing.domain.ports.out.InvoiceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InvoiceJpaRepository implements InvoiceRepository {

    private final InvoiceJpaRepositoryPort jpa;

    public InvoiceJpaRepository(InvoiceJpaRepositoryPort jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Invoice invoice) {
        jpa.save(toEntity(invoice));
    }

    @Override
    public Optional<Invoice> findById(InvoiceId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Invoice> findByShipmentId(String shipmentId) {
        return jpa.findByShipmentId(shipmentId).map(this::toDomain);
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return jpa.findByStatus(status.name()).stream().map(this::toDomain).toList();
    }

    private InvoiceJpaEntity toEntity(Invoice i) {
        InvoiceJpaEntity e = new InvoiceJpaEntity();
        e.id = i.getId().value();
        e.shipmentId = i.getShipmentId();
        e.shipperId = i.getShipperId();
        e.carrierId = i.getCarrierId();
        e.baseAmount = i.getBaseAmount().amount();
        e.baseCurrency = i.getBaseAmount().currency();
        e.penaltyDaysLate = i.getSlaPenalty().daysLate();
        e.penaltyAmount = i.getSlaPenalty().penaltyAmount().amount();
        e.penaltyCurrency = i.getSlaPenalty().penaltyAmount().currency();
        e.totalAmount = i.getTotalAmount().amount();
        e.totalCurrency = i.getTotalAmount().currency();
        e.dueDate = i.getDueDate();
        e.status = i.getStatus().name();
        return e;
    }

    private Invoice toDomain(InvoiceJpaEntity e) {
        Money base = new Money(e.baseAmount, e.baseCurrency);
        Money penaltyMoney = new Money(e.penaltyAmount, e.penaltyCurrency);
        SlaPenalty penalty = new SlaPenalty(e.penaltyDaysLate, penaltyMoney);
        Money total = new Money(e.totalAmount, e.totalCurrency);
        return Invoice.reconstitute(
                new InvoiceId(e.id), e.shipmentId, e.shipperId, e.carrierId,
                base, penalty, total, e.dueDate, InvoiceStatus.valueOf(e.status));
    }
}
