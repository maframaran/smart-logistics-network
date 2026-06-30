package com.logistics.billing.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.billing.domain.model.*;
import com.logistics.billing.domain.ports.out.InvoiceRepository;
import com.logistics.common.domain.DomainEvent;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InvoiceJpaRepository implements InvoiceRepository {

    private final InvoiceJpaRepositoryPort jpa;
    private final OutboxJpaRepositoryPort outboxJpa;
    private final ObjectMapper objectMapper;

    public InvoiceJpaRepository(InvoiceJpaRepositoryPort jpa, OutboxJpaRepositoryPort outboxJpa, ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.outboxJpa = outboxJpa;
        this.objectMapper = objectMapper;
    }

    // Writes the aggregate and its pulled domain events as outbox rows in the same
    // transaction (ADR-030) — atomic with the aggregate write since this method has
    // no @Transactional of its own and inherits the calling use case's boundary.
    @Override
    public void save(Invoice invoice) {
        jpa.save(toEntity(invoice));
        for (DomainEvent event : invoice.pullDomainEvents()) {
            outboxJpa.save(toOutboxEntity(event));
        }
    }

    private OutboxEventEntity toOutboxEntity(DomainEvent event) {
        OutboxEventEntity e = new OutboxEventEntity();
        e.aggregateId = event.aggregateId();
        e.eventType = event.getClass().getSimpleName();
        e.occurredAt = event.occurredAt();
        try {
            e.payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize domain event for outbox: " + event.getClass().getSimpleName(), ex);
        }
        return e;
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
    public List<Invoice> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

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
