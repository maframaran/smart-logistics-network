package com.logistics.billing.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "billing")
class InvoiceJpaEntity {

    @Id UUID id;
    @Column(nullable = false, unique = true) String shipmentId;
    @Column(nullable = false) String shipperId;
    @Column(nullable = false) String carrierId;
    @Column(nullable = false, precision = 12, scale = 2) BigDecimal baseAmount;
    @Column(nullable = false) String baseCurrency;
    @Column(nullable = false) long penaltyDaysLate;
    @Column(nullable = false, precision = 12, scale = 2) BigDecimal penaltyAmount;
    @Column(nullable = false) String penaltyCurrency;
    @Column(nullable = false, precision = 12, scale = 2) BigDecimal totalAmount;
    @Column(nullable = false) String totalCurrency;
    @Column(nullable = false) LocalDate dueDate;
    @Column(nullable = false) String status;
    @Version Long version;

    protected InvoiceJpaEntity() {}
}
