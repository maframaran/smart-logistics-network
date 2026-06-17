package com.logistics.billing.application.usecases;

import com.logistics.billing.domain.model.*;
import com.logistics.billing.domain.ports.in.GenerateInvoiceUseCase;
import com.logistics.billing.domain.ports.out.BillingEventPublisher;
import com.logistics.billing.domain.ports.out.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GenerateInvoiceService implements GenerateInvoiceUseCase {

    private final InvoiceRepository repository;
    private final BillingEventPublisher eventPublisher;

    public GenerateInvoiceService(InvoiceRepository repository, BillingEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
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

        repository.save(invoice);
        invoice.pullDomainEvents().forEach(eventPublisher::publish);
        return invoice.getId();
    }
}
