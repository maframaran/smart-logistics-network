package com.logistics.billing.domain.ports.in;

import com.logistics.billing.domain.model.Invoice;
import com.logistics.billing.domain.model.InvoiceId;
import com.logistics.billing.domain.model.InvoiceStatus;

import java.util.List;
import java.util.Optional;

public interface GetInvoiceUseCase {
    Invoice findById(InvoiceId id);
    Optional<Invoice> findByShipmentId(String shipmentId);
    List<Invoice> findByStatus(InvoiceStatus status);
}
