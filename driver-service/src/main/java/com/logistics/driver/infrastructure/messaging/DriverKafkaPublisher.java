package com.logistics.driver.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.driver.domain.events.DriverRegistered;
import com.logistics.driver.domain.events.DriverStatusChanged;
import com.logistics.driver.domain.ports.out.DriverEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DriverKafkaPublisher implements DriverEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<Class<? extends DomainEvent>, String> topics;

    public DriverKafkaPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topics.driver-registered}") String topicRegistered,
            @Value("${kafka.topics.driver-status-changed}") String topicStatusChanged) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = Map.of(
                DriverRegistered.class, topicRegistered,
                DriverStatusChanged.class, topicStatusChanged
        );
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = topics.get(event.getClass());
        if (topic == null) throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        kafkaTemplate.send(topic, event.aggregateId(), event);
    }
}
