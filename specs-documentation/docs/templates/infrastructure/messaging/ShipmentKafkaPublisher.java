package com.logistics.shipment.infrastructure.messaging;

import com.logistics.shipment.domain.events.ShipmentCreated;
import com.logistics.shipment.domain.ports.out.ShipmentEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// Outbound Adapter — implements ShipmentEventPublisher domain port.
// Serialises the domain event to JSON and publishes to Kafka.
// Payload schema must match messaging/topics/shipment.created.md exactly.
@Component
public class ShipmentKafkaPublisher implements ShipmentEventPublisher {

    // Topic name matches messaging/topics/shipment.created.md
    private static final String TOPIC = "shipment.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ShipmentKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(ShipmentCreated event) {
        // Partition key = shipmentId (spec: messaging/topics/shipment.created.md § Partition key)
        kafkaTemplate.send(TOPIC, event.shipmentId().toString(), toPayload(event));
    }

    // Map domain event fields to the JSON payload schema from messaging/topics/shipment.created.md
    private ShipmentCreatedPayload toPayload(ShipmentCreated event) {
        return new ShipmentCreatedPayload(
                event.eventId().toString(),
                event.eventVersion(),
                "ShipmentCreated",
                event.occurredAt().toString(),
                event.shipmentId().toString(),
                event.cargoSpec().weightKg(),
                event.cargoSpec().volumeM3(),
                event.cargoSpec().requiresHazmat(),
                event.cargoSpec().requiresColdChain(),
                event.slaType().name(),
                event.requiredDeliveryDate().toString()
        );
    }

    // DTO matching the Kafka payload schema — not a domain type
    record ShipmentCreatedPayload(
            String eventId,
            int eventVersion,
            String eventType,
            String occurredAt,
            String shipmentId,
            double weightKg,
            double volumeM3,
            boolean requiresHazmat,
            boolean requiresColdChain,
            String slaType,
            String requiredDeliveryDate
    ) {}
}
