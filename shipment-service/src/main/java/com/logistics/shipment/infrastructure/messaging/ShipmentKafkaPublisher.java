package com.logistics.shipment.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.shipment.domain.events.ShipmentAssigned;
import com.logistics.shipment.domain.events.ShipmentCancelled;
import com.logistics.shipment.domain.events.ShipmentCreated;
import com.logistics.shipment.domain.ports.out.ShipmentEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ShipmentKafkaPublisher implements ShipmentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<Class<? extends DomainEvent>, String> topics;

    public ShipmentKafkaPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topics.shipment-created}") String topicCreated,
            @Value("${kafka.topics.shipment-assigned}") String topicAssigned,
            @Value("${kafka.topics.shipment-cancelled}") String topicCancelled) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = Map.of(
                ShipmentCreated.class, topicCreated,
                ShipmentAssigned.class, topicAssigned,
                ShipmentCancelled.class, topicCancelled
        );
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = topics.get(event.getClass());
        if (topic == null) throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        kafkaTemplate.send(topic, event.aggregateId(), event);
    }
}
