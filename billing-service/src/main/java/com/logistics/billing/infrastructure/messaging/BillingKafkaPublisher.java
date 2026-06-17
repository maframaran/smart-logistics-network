package com.logistics.billing.infrastructure.messaging;

import com.logistics.billing.domain.events.InvoiceGenerated;
import com.logistics.billing.domain.events.InvoicePaid;
import com.logistics.billing.domain.ports.out.BillingEventPublisher;
import com.logistics.common.domain.DomainEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BillingKafkaPublisher implements BillingEventPublisher {

    private static final String TOPIC_GENERATED = "billing.invoice-generated";
    private static final String TOPIC_PAID      = "billing.invoice-paid";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BillingKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = switch (event) {
            case InvoiceGenerated ignored -> TOPIC_GENERATED;
            case InvoicePaid      ignored -> TOPIC_PAID;
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        };
        kafkaTemplate.send(topic, event.aggregateId(), event);
    }
}
