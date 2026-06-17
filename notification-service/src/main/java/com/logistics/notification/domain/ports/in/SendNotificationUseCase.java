package com.logistics.notification.domain.ports.in;

import com.logistics.notification.domain.model.NotificationChannel;
import com.logistics.notification.domain.model.NotificationId;
import com.logistics.notification.domain.model.NotificationType;

public interface SendNotificationUseCase {

    NotificationId send(Command command);

    record Command(
            NotificationType type,
            NotificationChannel channel,
            String recipientAddress,
            String recipientName,
            String subject,
            String body,
            String referenceId
    ) {}
}
