package com.logistics.notification.domain.model;

import com.logistics.common.domain.AggregateRoot;
import com.logistics.notification.domain.events.NotificationSent;
import com.logistics.notification.domain.events.NotificationFailed;

import java.time.Instant;

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

        return new Notification(NotificationId.generate(), type, channel,
                recipientAddress, recipientName, subject, body, referenceId,
                NotificationStatus.PENDING, null, Instant.now());
    }

    public static Notification reconstitute(NotificationId id, NotificationType type, NotificationChannel channel,
                                             String recipientAddress, String recipientName,
                                             String subject, String body, String referenceId,
                                             NotificationStatus status, String failureReason, Instant createdAt) {
        return new Notification(id, type, channel, recipientAddress, recipientName,
                subject, body, referenceId, status, failureReason, createdAt);
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

    public NotificationId getId() { return id; }
    public NotificationType getType() { return type; }
    public NotificationChannel getChannel() { return channel; }
    public String getRecipientAddress() { return recipientAddress; }
    public String getRecipientName() { return recipientName; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getReferenceId() { return referenceId; }
    public NotificationStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
}
