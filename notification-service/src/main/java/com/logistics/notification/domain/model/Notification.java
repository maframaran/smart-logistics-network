package com.logistics.notification.domain.model;

import com.logistics.common.domain.AggregateRoot;
import com.logistics.notification.domain.events.NotificationSent;
import com.logistics.notification.domain.events.NotificationFailed;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
public class Notification extends AggregateRoot {

    private final NotificationId id;
    private final NotificationType type;
    private final NotificationChannel channel;
    private final String recipientAddress; // email or phone
    private final String recipientName;
    private final String subject;
    private final String body;
    private final String referenceId;     // shipmentId, invoiceId, etc.
    private NotificationStatus status;
    private String failureReason;
    private final Instant createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Notification(NotificationId id, NotificationType type, NotificationChannel channel,
                         String recipientAddress, String recipientName,
                         String subject, String body, String referenceId,
                         NotificationStatus status, String failureReason, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.channel = channel;
        this.recipientAddress = recipientAddress;
        this.recipientName = recipientName;
        this.subject = subject;
        this.body = body;
        this.referenceId = referenceId;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
    }

    public static Notification create(NotificationType type, NotificationChannel channel,
                                      String recipientAddress, String recipientName,
                                      String subject, String body, String referenceId) {
        if (recipientAddress == null || recipientAddress.isBlank())
            throw new IllegalArgumentException("recipientAddress must not be blank");
        if (body == null || body.isBlank())
            throw new IllegalArgumentException("body must not be blank");

        return Notification.builder()
                .id(NotificationId.generate()).type(type).channel(channel)
                .recipientAddress(recipientAddress).recipientName(recipientName)
                .subject(subject).body(body).referenceId(referenceId)
                .status(NotificationStatus.PENDING).failureReason(null).createdAt(Instant.now())
                .build();
    }

    public static Notification reconstitute(NotificationId id, NotificationType type, NotificationChannel channel,
                                             String recipientAddress, String recipientName,
                                             String subject, String body, String referenceId,
                                             NotificationStatus status, String failureReason, Instant createdAt) {
        return Notification.builder()
                .id(id).type(type).channel(channel)
                .recipientAddress(recipientAddress).recipientName(recipientName)
                .subject(subject).body(body).referenceId(referenceId)
                .status(status).failureReason(failureReason).createdAt(createdAt)
                .build();
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        registerEvent(NotificationSent.of(id.toString(), type, channel, recipientAddress, referenceId));
    }

    public void markFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
        registerEvent(NotificationFailed.of(id.toString(), type, channel, recipientAddress, referenceId, reason));
    }
}
