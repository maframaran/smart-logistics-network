package com.logistics.routing.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.routing.domain.events.RouteCalculated;
import com.logistics.routing.domain.ports.out.RouteEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RouteKafkaPublisher implements RouteEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<Class<? extends DomainEvent>, String> topics;

    public RouteKafkaPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topics.route-calculated}") String topicCalculated) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = Map.of(RouteCalculated.class, topicCalculated);
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = topics.get(event.getClass());
        if (topic == null) throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        kafkaTemplate.send(topic, event.aggregateId(), event);
    }
}
