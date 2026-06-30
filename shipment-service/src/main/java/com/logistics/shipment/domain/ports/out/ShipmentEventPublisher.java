package com.logistics.shipment.domain.ports.out;

import com.logistics.common.domain.DomainEvent;

import java.util.concurrent.CompletableFuture;

public interface ShipmentEventPublisher {

    // Returns a future completing when the broker has acknowledged the event (or
    // failing if it didn't), so the outbox relay (ADR-030) can confirm delivery
    // before marking a row published. CompletableFuture<Void>, not a Kafka-specific
    // result type, to keep this domain port free of infrastructure imports.
    CompletableFuture<Void> publish(DomainEvent event);
}
