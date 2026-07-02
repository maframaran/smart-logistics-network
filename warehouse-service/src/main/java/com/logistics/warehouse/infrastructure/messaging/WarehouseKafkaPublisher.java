package com.logistics.warehouse.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.warehouse.domain.events.*;
import com.logistics.warehouse.domain.ports.out.WarehouseEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class WarehouseKafkaPublisher implements WarehouseEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WarehouseEventAvroMapper avroMapper;
    private final Map<Class<? extends DomainEvent>, String> topics;

    public WarehouseKafkaPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            WarehouseEventAvroMapper avroMapper,
            @Value("${kafka.topics.inventory-received}") String topicReceived,
            @Value("${kafka.topics.inventory-dispatched}") String topicDispatched,
            @Value("${kafka.topics.warehouse-capacity-updated}") String topicCapacityUpdated) {
        this.kafkaTemplate = kafkaTemplate;
        this.avroMapper = avroMapper;
        this.topics = Map.of(
                InventoryReceived.class, topicReceived,
                InventoryDispatched.class, topicDispatched,
                WarehouseCapacityUpdated.class, topicCapacityUpdated
        );
    }

    @Override
    public CompletableFuture<Void> publish(DomainEvent event) {
        String topic = topics.get(event.getClass());
        if (topic == null) throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        return kafkaTemplate.send(topic, event.aggregateId(), avroMapper.toAvro(event)).thenAccept(result -> { });
    }
}
