package com.logistics.warehouse.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.warehouse.domain.events.*;
import com.logistics.warehouse.domain.ports.out.WarehouseEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class WarehouseKafkaPublisher implements WarehouseEventPublisher {

    private static final String TOPIC_RECEIVED          = "warehouse.inventory-received";
    private static final String TOPIC_DISPATCHED        = "warehouse.inventory-dispatched";
    private static final String TOPIC_CAPACITY_UPDATED  = "warehouse.capacity-updated";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public WarehouseKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = switch (event) {
            case InventoryReceived       ignored -> TOPIC_RECEIVED;
            case InventoryDispatched     ignored -> TOPIC_DISPATCHED;
            case WarehouseCapacityUpdated ignored -> TOPIC_CAPACITY_UPDATED;
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        };
        kafkaTemplate.send(topic, event.aggregateId(), event);
    }
}
