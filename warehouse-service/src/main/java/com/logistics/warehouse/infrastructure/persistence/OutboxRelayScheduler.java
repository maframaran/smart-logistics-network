package com.logistics.warehouse.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.domain.DomainEvent;
import com.logistics.warehouse.domain.events.InventoryDispatched;
import com.logistics.warehouse.domain.events.InventoryReceived;
import com.logistics.warehouse.domain.events.WarehouseCapacityUpdated;
import com.logistics.warehouse.domain.ports.out.WarehouseEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Polls unpublished outbox rows and relays them to Kafka, closing the at-least-once
// delivery gap left by KafkaTemplate's non-blocking send (ADR-030). Blocks briefly per
// row to await broker acknowledgment before marking it published. Lives in
// infrastructure/persistence (not infrastructure/messaging) so it can access the
// package-private OutboxEventEntity/OutboxJpaRepositoryPort without widening their visibility.
@Component
class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);

    private static final Map<String, Class<? extends DomainEvent>> EVENT_TYPES = Map.of(
            "InventoryReceived", InventoryReceived.class,
            "InventoryDispatched", InventoryDispatched.class,
            "WarehouseCapacityUpdated", WarehouseCapacityUpdated.class
    );

    private final OutboxJpaRepositoryPort outboxJpa;
    private final WarehouseEventPublisher publisher;
    private final ObjectMapper objectMapper;

    OutboxRelayScheduler(OutboxJpaRepositoryPort outboxJpa, WarehouseEventPublisher publisher, ObjectMapper objectMapper) {
        this.outboxJpa = outboxJpa;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.poll-interval-ms:500}")
    @Transactional
    void relay() {
        List<OutboxEventEntity> unpublished = outboxJpa.findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();
        for (OutboxEventEntity row : unpublished) {
            relayOne(row);
        }
    }

    private void relayOne(OutboxEventEntity row) {
        Class<? extends DomainEvent> type = EVENT_TYPES.get(row.eventType);
        if (type == null) {
            log.error("Unknown outbox event type, skipping: {} (id={})", row.eventType, row.id);
            return;
        }
        try {
            DomainEvent event = objectMapper.readValue(row.payload, type);
            publisher.publish(event).get(10, TimeUnit.SECONDS);
            row.publishedAt = Instant.now();
            outboxJpa.save(row);
        } catch (Exception e) {
            log.error("Failed to relay outbox event {} (id={}): {}", row.eventType, row.id, e.getMessage());
            // left unpublished — retried on the next poll
        }
    }
}
