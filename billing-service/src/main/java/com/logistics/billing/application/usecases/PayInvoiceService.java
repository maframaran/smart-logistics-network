package com.logistics.billing.application.usecases;

import com.logistics.billing.domain.model.Invoice;
import com.logistics.billing.domain.model.InvoiceId;
import com.logistics.billing.domain.ports.in.PayInvoiceUseCase;
import com.logistics.billing.domain.ports.out.BillingEventPublisher;
import com.logistics.billing.domain.ports.out.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PayInvoiceService implements PayInvoiceUseCase {

    private final InvoiceRepository repository;
    private final BillingEventPublisher eventPublisher;

    public PayInvoiceService(InvoiceRepository repository, BillingEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void pay(InvoiceId invoiceId) {
        Invoice invoice = repository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        invoice.markPaid();
        repository.save(invoice);
        invoice.pullDomainEvents().forEach(eventPublisher::publish);
    }
}
