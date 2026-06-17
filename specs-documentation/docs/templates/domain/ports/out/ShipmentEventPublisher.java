package com.logistics.shipment.domain.ports.out;

import com.logistics.shipment.domain.events.ShipmentCreated;

// Outbound Port — spec: specs/features/F-001-create-shipment.md § AC-002
// Implemented by ShipmentKafkaPublisher (infrastructure/messaging).
// One method per domain event type — keeps the interface explicit and testable.
public interface ShipmentEventPublisher {

    // Publishes to topic: shipment.created (see messaging/topics/shipment.created.md)
    void publish(ShipmentCreated event);
}
