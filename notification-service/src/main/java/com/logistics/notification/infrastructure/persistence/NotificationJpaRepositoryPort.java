package com.logistics.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface NotificationJpaRepositoryPort extends JpaRepository<NotificationJpaEntity, UUID> {
    List<NotificationJpaEntity> findByReferenceId(String referenceId);
    List<NotificationJpaEntity> findByStatus(String status);
}
