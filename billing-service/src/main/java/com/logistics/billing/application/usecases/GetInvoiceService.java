package com.logistics.billing.application.usecases;

import com.logistics.billing.domain.model.Invoice;
import com.logistics.billing.domain.model.InvoiceId;
import com.logistics.billing.domain.model.InvoiceStatus;
import com.logistics.billing.domain.ports.in.GetInvoiceUseCase;
import com.logistics.billing.domain.ports.out.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetInvoiceService implements GetInvoiceUseCase {

    private final InvoiceRepository repository;

    public GetInvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Invoice findById(InvoiceId id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));
    }

    @Override
    public Optional<Invoice> findByShipmentId(String shipmentId) {
        return repository.findByShipmentId(shipmentId);
    }

    @Override
    public List<Invoice> findAll() {
        return repository.findAll();
    }

    public List<Invoice> findByStatus(InvoiceStatus status) {
        return repository.findByStatus(status);
    }
}
