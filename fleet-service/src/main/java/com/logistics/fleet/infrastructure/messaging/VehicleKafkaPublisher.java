package com.logistics.fleet.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.fleet.domain.events.VehicleRegistered;
import com.logistics.fleet.domain.events.VehicleStatusChanged;
import com.logistics.fleet.domain.ports.out.VehicleEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class VehicleKafkaPublisher implements VehicleEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FleetEventAvroMapper avroMapper;
    private final Map<Class<? extends DomainEvent>, String> topics;

    public VehicleKafkaPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            FleetEventAvroMapper avroMapper,
            @Value("${kafka.topics.vehicle-registered}") String topicRegistered,
            @Value("${kafka.topics.vehicle-status-changed}") String topicStatusChanged) {
        this.kafkaTemplate = kafkaTemplate;
        this.avroMapper = avroMapper;
        this.topics = Map.of(
                VehicleRegistered.class, topicRegistered,
                VehicleStatusChanged.class, topicStatusChanged
        );
    }

    @Override
    public CompletableFuture<Void> publish(DomainEvent event) {
        String topic = topics.get(event.getClass());
        if (topic == null) throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        return kafkaTemplate.send(topic, event.aggregateId(), avroMapper.toAvro(event)).thenAccept(result -> { });
    }
}
