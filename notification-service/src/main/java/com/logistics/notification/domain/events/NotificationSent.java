package com.logistics.notification.domain.events;

import com.logistics.common.domain.DomainEvent;
import com.logistics.notification.domain.model.NotificationChannel;
import com.logistics.notification.domain.model.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationSent(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        NotificationType notificationType,
        NotificationChannel channel,
        String recipientAddress,
        String referenceId
) implements DomainEvent {

    public static NotificationSent of(String notificationId, NotificationType type,
                                       NotificationChannel channel, String recipient, String referenceId) {
        return new NotificationSent(UUID.randomUUID(), Instant.now(), notificationId,
                type, channel, recipient, referenceId);
    }
}
