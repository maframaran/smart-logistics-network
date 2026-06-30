package com.logistics.driver.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// Transactional Outbox row (ADR-030). Not an aggregate — a plain persistence record
// written atomically alongside the aggregate's own row, then relayed to Kafka async
// by OutboxRelayScheduler.
@Entity
@Table(name = "outbox_events", schema = "driver")
class OutboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String aggregateId;

    @Column(nullable = false)
    String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    String payload;

    @Column(nullable = false)
    Instant occurredAt;

    Instant publishedAt;
}
