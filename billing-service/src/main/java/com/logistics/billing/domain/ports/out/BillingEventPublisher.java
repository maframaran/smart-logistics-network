package com.logistics.billing.domain.ports.out;

import com.logistics.common.domain.DomainEvent;

import java.util.concurrent.CompletableFuture;

public interface BillingEventPublisher {

    // Returns a future completing when the broker has acknowledged the event (or
    // failing if it didn't), so the outbox relay (ADR-030) can confirm delivery
    // before marking a row published.
    CompletableFuture<Void> publish(DomainEvent event);
}
