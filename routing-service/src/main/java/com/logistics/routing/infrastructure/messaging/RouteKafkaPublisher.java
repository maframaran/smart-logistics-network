package com.logistics.routing.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.routing.domain.events.RouteCalculated;
import com.logistics.routing.domain.ports.out.RouteEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class RouteKafkaPublisher implements RouteEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RoutingEventAvroMapper avroMapper;
    private final Map<Class<? extends DomainEvent>, String> topics;

    public RouteKafkaPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            RoutingEventAvroMapper avroMapper,
            @Value("${kafka.topics.route-calculated}") String topicCalculated) {
        this.kafkaTemplate = kafkaTemplate;
        this.avroMapper = avroMapper;
        this.topics = Map.of(RouteCalculated.class, topicCalculated);
    }

    @Override
    public CompletableFuture<Void> publish(DomainEvent event) {
        String topic = topics.get(event.getClass());
        if (topic == null) throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        return kafkaTemplate.send(topic, event.aggregateId(), avroMapper.toAvro(event)).thenAccept(result -> { });
    }
}
