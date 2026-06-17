package com.logistics.billing.domain;

import com.logistics.billing.domain.events.InvoiceGenerated;
import com.logistics.billing.domain.events.InvoicePaid;
import com.logistics.billing.domain.model.*;
import com.logistics.common.domain.DomainEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class InvoiceTest {

    private static final LocalDate DUE = LocalDate.now().plusDays(30);
    private static final Money BASE = Money.brl(500.0);

    @Test
    void generate_onTime_noSlaPenalty() {
        LocalDate promised = LocalDate.now().minusDays(1);
        LocalDate actual   = LocalDate.now().minusDays(1); // delivered on promised day

        SlaPenalty penalty = SlaPenalty.calculate(promised, actual, SlaType.STANDARD);
        Invoice invoice = Invoice.generate("ship-1", "shipper-1", "carrier-1", BASE, penalty, DUE);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
        assertThat(invoice.getSlaPenalty().applies()).isFalse();
        assertThat(invoice.getTotalAmount().amount()).isEqualByComparingTo(BASE.amount());

        List<DomainEvent> events = invoice.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(InvoiceGenerated.class);
    }

    @Test
    void generate_lateDelivery_standardPenalty() {
        LocalDate promised = LocalDate.now().minusDays(3);
        LocalDate actual   = LocalDate.now(); // 3 days late

        SlaPenalty penalty = SlaPenalty.calculate(promised, actual, SlaType.STANDARD);
        Invoice invoice = Invoice.generate("ship-2", "shipper-1", "carrier-1", BASE, penalty, DUE);

        assertThat(penalty.daysLate()).isEqualTo(3);
        assertThat(penalty.penaltyAmount().amount().doubleValue()).isEqualTo(150.0); // 3 * 50
        assertThat(invoice.getTotalAmount().amount().doubleValue()).isEqualTo(650.0);
    }

    @Test
    void generate_lateDelivery_expressPenalty() {
        LocalDate promised = LocalDate.now().minusDays(2);
        LocalDate actual   = LocalDate.now(); // 2 days late

        SlaPenalty penalty = SlaPenalty.calculate(promised, actual, SlaType.EXPRESS);
        Invoice invoice = Invoice.generate("ship-3", "s1", "c1", BASE, penalty, DUE);

        assertThat(penalty.penaltyAmount().amount().doubleValue()).isEqualTo(600.0); // 2 * 300
        assertThat(invoice.getTotalAmount().amount().doubleValue()).isEqualTo(1100.0);
    }

    @Test
    void markPaid_transitionsToPaidAndRaisesEvent() {
        SlaPenalty noPenalty = new SlaPenalty(0, Money.brl(0));
        Invoice invoice = Invoice.generate("ship-4", "s1", "c1", BASE, noPenalty, DUE);
        invoice.pullDomainEvents();

        invoice.markPaid();

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.pullDomainEvents().getFirst()).isInstanceOf(InvoicePaid.class);
    }

    @Test
    void markPaid_alreadyPaid_throws() {
        SlaPenalty noPenalty = new SlaPenalty(0, Money.brl(0));
        Invoice invoice = Invoice.generate("ship-5", "s1", "c1", BASE, noPenalty, DUE);
        invoice.markPaid();
        invoice.pullDomainEvents();

        assertThatThrownBy(invoice::markPaid)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot pay invoice");
    }

    @Test
    void slaPenalty_earlyDelivery_noPenalty() {
        LocalDate promised = LocalDate.now().plusDays(5);
        LocalDate actual   = LocalDate.now().plusDays(3); // 2 days early

        SlaPenalty penalty = SlaPenalty.calculate(promised, actual, SlaType.PRIORITY);
        assertThat(penalty.applies()).isFalse();
        assertThat(penalty.daysLate()).isEqualTo(0);
    }

    @Test
    void money_add_samesCurrency() {
        Money a = Money.brl(100.0);
        Money b = Money.brl(50.50);
        assertThat(a.add(b).amount().doubleValue()).isEqualTo(150.50);
    }

    @Test
    void money_add_differentCurrency_throws() {
        Money brl = Money.brl(100.0);
        Money usd = new Money(java.math.BigDecimal.valueOf(100.0), "USD");
        assertThatThrownBy(() -> brl.add(usd)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generate_withBlankShipmentId_throws() {
        SlaPenalty noPenalty = new SlaPenalty(0, Money.brl(0));
        assertThatThrownBy(() -> Invoice.generate("", "s1", "c1", BASE, noPenalty, DUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("shipmentId");
    }
}
