package com.logistics.notification.infrastructure.persistence;

import com.logistics.notification.domain.model.*;
import com.logistics.notification.domain.ports.out.NotificationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NotificationJpaRepository implements NotificationRepository {

    private final NotificationJpaRepositoryPort jpa;

    public NotificationJpaRepository(NotificationJpaRepositoryPort jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Notification n) {
        jpa.save(toEntity(n));
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Notification> findByReferenceId(String referenceId) {
        return jpa.findByReferenceId(referenceId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Notification> findByStatus(NotificationStatus status) {
        return jpa.findByStatus(status.name()).stream().map(this::toDomain).toList();
    }

    private NotificationJpaEntity toEntity(Notification n) {
        NotificationJpaEntity e = new NotificationJpaEntity();
        e.id = n.getId().value();
        e.type = n.getType().name();
        e.channel = n.getChannel().name();
        e.recipientAddress = n.getRecipientAddress();
        e.recipientName = n.getRecipientName();
        e.subject = n.getSubject();
        e.body = n.getBody();
        e.referenceId = n.getReferenceId();
        e.status = n.getStatus().name();
        e.failureReason = n.getFailureReason();
        e.createdAt = n.getCreatedAt();
        return e;
    }

    private Notification toDomain(NotificationJpaEntity e) {
        return Notification.reconstitute(
                new NotificationId(e.id),
                NotificationType.valueOf(e.type),
                NotificationChannel.valueOf(e.channel),
                e.recipientAddress, e.recipientName,
                e.subject, e.body, e.referenceId,
                NotificationStatus.valueOf(e.status),
                e.failureReason, e.createdAt);
    }
}
