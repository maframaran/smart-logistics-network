package com.logistics.notification.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", schema = "notification")
class NotificationJpaEntity {

    @Id UUID id;
    @Column(nullable = false) String type;
    @Column(nullable = false) String channel;
    @Column(nullable = false) String recipientAddress;
    String recipientName;
    String subject;
    @Column(nullable = false, columnDefinition = "TEXT") String body;
    @Column(nullable = false) String referenceId;
    @Column(nullable = false) String status;
    String failureReason;
    @Column(nullable = false) Instant createdAt;
    @Version Long version;

    protected NotificationJpaEntity() {}
}
