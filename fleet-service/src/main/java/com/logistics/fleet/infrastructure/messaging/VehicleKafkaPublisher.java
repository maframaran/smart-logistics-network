package com.logistics.fleet.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.fleet.domain.events.VehicleRegistered;
import com.logistics.fleet.domain.events.VehicleStatusChanged;
import com.logistics.fleet.domain.ports.out.VehicleEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class VehicleKafkaPublisher implements VehicleEventPublisher {

    private static final String TOPIC_REGISTERED      = "fleet.vehicle-registered";
    private static final String TOPIC_STATUS_CHANGED  = "fleet.vehicle-status-changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public VehicleKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = switch (event) {
            case VehicleRegistered    ignored -> TOPIC_REGISTERED;
            case VehicleStatusChanged ignored -> TOPIC_STATUS_CHANGED;
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        };
        kafkaTemplate.send(topic, event.aggregateId(), event);
    }
}
