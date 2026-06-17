package com.logistics.billing.domain.ports.in;

import com.logistics.billing.domain.model.InvoiceId;

public interface PayInvoiceUseCase {
    void pay(InvoiceId invoiceId);
}
