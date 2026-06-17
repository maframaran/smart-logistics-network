package com.logistics.driver.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.driver.domain.events.DriverRegistered;
import com.logistics.driver.domain.events.DriverStatusChanged;
import com.logistics.driver.domain.ports.out.DriverEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DriverKafkaPublisher implements DriverEventPublisher {

    private static final String TOPIC_REGISTERED     = "fleet.driver-registered";
    private static final String TOPIC_STATUS_CHANGED = "fleet.driver-status-changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DriverKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = switch (event) {
            case DriverRegistered    ignored -> TOPIC_REGISTERED;
            case DriverStatusChanged ignored -> TOPIC_STATUS_CHANGED;
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        };
        kafkaTemplate.send(topic, event.aggregateId(), event);
    }
}
