package com.logistics.notification.domain;

import com.logistics.notification.domain.events.NotificationFailed;
import com.logistics.notification.domain.events.NotificationSent;
import com.logistics.notification.domain.model.*;
import com.logistics.common.domain.DomainEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class NotificationTest {

    private Notification notification() {
        return Notification.create(
                NotificationType.SHIPMENT_CREATED,
                NotificationChannel.EMAIL,
                "recipient@example.com",
                "João Silva",
                "Shipment Created",
                "Your shipment has been created.",
                "ship-abc-123"
        );
    }

    @Test
    void create_initialStatusIsPending() {
        Notification n = notification();
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(n.getFailureReason()).isNull();
    }

    @Test
    void markSent_transitionsToSentAndRaisesEvent() {
        Notification n = notification();

        n.markSent();

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
        List<DomainEvent> events = n.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(NotificationSent.class);
    }

    @Test
    void markFailed_transitionsToFailedAndRaisesEvent() {
        Notification n = notification();

        n.markFailed("SMTP connection refused");

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(n.getFailureReason()).isEqualTo("SMTP connection refused");
        List<DomainEvent> events = n.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(NotificationFailed.class);
    }

    @Test
    void create_withBlankRecipient_throws() {
        assertThatThrownBy(() -> Notification.create(
                NotificationType.SHIPMENT_DELIVERED, NotificationChannel.EMAIL,
                "", "Name", "Subject", "Body", "ref-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("recipientAddress");
    }

    @Test
    void create_withBlankBody_throws() {
        assertThatThrownBy(() -> Notification.create(
                NotificationType.SHIPMENT_ASSIGNED, NotificationChannel.EMAIL,
                "test@test.com", "Name", "Subject", "", "ref-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("body");
    }

    @Test
    void pullDomainEvents_clearsEvents() {
        Notification n = notification();
        n.markSent();
        n.pullDomainEvents();
        assertThat(n.pullDomainEvents()).isEmpty();
    }
}
