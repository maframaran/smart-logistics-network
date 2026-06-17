package com.logistics.billing.domain.ports.in;

import com.logistics.billing.domain.model.InvoiceId;
import com.logistics.billing.domain.model.Money;
import com.logistics.billing.domain.model.SlaType;

import java.time.LocalDate;

public interface GenerateInvoiceUseCase {
    InvoiceId generate(Command command);

    record Command(
            String shipmentId,
            String shipperId,
            String carrierId,
            Money baseAmount,
            SlaType slaType,
            LocalDate promisedDeliveryDate,
            LocalDate actualDeliveryDate,
            LocalDate dueDate
    ) {}
}
