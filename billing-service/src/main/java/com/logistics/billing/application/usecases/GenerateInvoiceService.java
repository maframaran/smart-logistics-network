package com.logistics.billing.application.usecases;

import com.logistics.billing.domain.model.*;
import com.logistics.billing.domain.ports.in.GenerateInvoiceUseCase;
import com.logistics.billing.domain.ports.out.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GenerateInvoiceService implements GenerateInvoiceUseCase {

    private final InvoiceRepository repository;

    public GenerateInvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public InvoiceId generate(Command command) {
        SlaPenalty penalty = SlaPenalty.calculate(
                command.promisedDeliveryDate(),
                command.actualDeliveryDate(),
                command.slaType());

        Invoice invoice = Invoice.generate(
                command.shipmentId(), command.shipperId(), command.carrierId(),
                command.baseAmount(), penalty, command.dueDate());

        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(invoice);
        return invoice.getId();
    }
}
