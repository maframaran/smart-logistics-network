package com.logistics.billing.application.usecases;

import com.logistics.billing.domain.model.Invoice;
import com.logistics.billing.domain.model.InvoiceId;
import com.logistics.billing.domain.ports.in.PayInvoiceUseCase;
import com.logistics.billing.domain.ports.out.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PayInvoiceService implements PayInvoiceUseCase {

    private final InvoiceRepository repository;

    public PayInvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void pay(InvoiceId invoiceId) {
        Invoice invoice = repository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        invoice.markPaid();
        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(invoice);
    }
}
