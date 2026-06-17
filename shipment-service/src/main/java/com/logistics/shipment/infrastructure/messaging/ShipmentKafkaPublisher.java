package com.logistics.shipment.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.shipment.domain.events.ShipmentAssigned;
import com.logistics.shipment.domain.events.ShipmentCancelled;
import com.logistics.shipment.domain.events.ShipmentCreated;
import com.logistics.shipment.domain.ports.out.ShipmentEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShipmentKafkaPublisher implements ShipmentEventPublisher {

    private static final String TOPIC_CREATED   = "shipment.created";
    private static final String TOPIC_ASSIGNED  = "shipment.assigned";
    private static final String TOPIC_CANCELLED = "shipment.cancelled";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ShipmentKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = topicFor(event);
        String key   = event.aggregateId();
        kafkaTemplate.send(topic, key, event);
    }

    private String topicFor(DomainEvent event) {
        return switch (event) {
            case ShipmentCreated   ignored -> TOPIC_CREATED;
            case ShipmentAssigned  ignored -> TOPIC_ASSIGNED;
            case ShipmentCancelled ignored -> TOPIC_CANCELLED;
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        };
    }
}
