package com.logistics.routing.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.routing.domain.events.RouteCalculated;
import com.logistics.routing.domain.ports.out.RouteEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RouteKafkaPublisher implements RouteEventPublisher {

    private static final String TOPIC = "routing.route-calculated";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RouteKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        if (!(event instanceof RouteCalculated)) {
            throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        }
        kafkaTemplate.send(TOPIC, event.aggregateId(), event);
    }
}
