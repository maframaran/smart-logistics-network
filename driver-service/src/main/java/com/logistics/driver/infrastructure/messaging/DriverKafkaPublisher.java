package com.logistics.driver.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.driver.domain.events.DriverRegistered;
import com.logistics.driver.domain.events.DriverStatusChanged;
import com.logistics.driver.domain.ports.out.DriverEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class DriverKafkaPublisher implements DriverEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DriverEventAvroMapper avroMapper;
    private final Map<Class<? extends DomainEvent>, String> topics;

    public DriverKafkaPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            DriverEventAvroMapper avroMapper,
            @Value("${kafka.topics.driver-registered}") String topicRegistered,
            @Value("${kafka.topics.driver-status-changed}") String topicStatusChanged) {
        this.kafkaTemplate = kafkaTemplate;
        this.avroMapper = avroMapper;
        this.topics = Map.of(
                DriverRegistered.class, topicRegistered,
                DriverStatusChanged.class, topicStatusChanged
        );
    }

    @Override
    public CompletableFuture<Void> publish(DomainEvent event) {
        String topic = topics.get(event.getClass());
        if (topic == null) throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        return kafkaTemplate.send(topic, event.aggregateId(), avroMapper.toAvro(event)).thenAccept(result -> { });
    }
}
