package com.logistics.billing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface OutboxJpaRepositoryPort extends JpaRepository<OutboxEventEntity, Long> {
    List<OutboxEventEntity> findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();
}
