package com.logistics.billing.infrastructure.messaging;

import com.logistics.billing.domain.events.InvoiceGenerated;
import com.logistics.billing.domain.events.InvoicePaid;
import com.logistics.billing.domain.ports.out.BillingEventPublisher;
import com.logistics.common.domain.DomainEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class BillingKafkaPublisher implements BillingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<Class<? extends DomainEvent>, String> topics;

    public BillingKafkaPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topics.invoice-generated}") String topicGenerated,
            @Value("${kafka.topics.invoice-paid}") String topicPaid) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = Map.of(
                InvoiceGenerated.class, topicGenerated,
                InvoicePaid.class, topicPaid
        );
    }

    @Override
    public CompletableFuture<Void> publish(DomainEvent event) {
        String topic = topics.get(event.getClass());
        if (topic == null) throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        return kafkaTemplate.send(topic, event.aggregateId(), event).thenAccept(result -> { });
    }
}
