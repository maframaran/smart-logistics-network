package com.logistics.notification.domain.events;

import com.logistics.common.domain.DomainEvent;
import com.logistics.notification.domain.model.NotificationChannel;
import com.logistics.notification.domain.model.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationFailed(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        NotificationType notificationType,
        NotificationChannel channel,
        String recipientAddress,
        String referenceId,
        String reason
) implements DomainEvent {

    public static NotificationFailed of(String notificationId, NotificationType type,
                                         NotificationChannel channel, String recipient,
                                         String referenceId, String reason) {
        return new NotificationFailed(UUID.randomUUID(), Instant.now(), notificationId,
                type, channel, recipient, referenceId, reason);
    }
}
