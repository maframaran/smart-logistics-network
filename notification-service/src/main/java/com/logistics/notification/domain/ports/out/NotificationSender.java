package com.logistics.notification.domain.ports.out;

import com.logistics.notification.domain.model.Notification;

// Outbound port — implemented by email/SMS adapters
public interface NotificationSender {

    void send(Notification notification);

    boolean supports(com.logistics.notification.domain.model.NotificationChannel channel);
}
