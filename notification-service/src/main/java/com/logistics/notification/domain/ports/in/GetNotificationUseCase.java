package com.logistics.notification.domain.ports.in;

import com.logistics.notification.domain.model.Notification;
import com.logistics.notification.domain.model.NotificationId;
import com.logistics.notification.domain.model.NotificationStatus;

import java.util.List;

public interface GetNotificationUseCase {
    Notification findById(NotificationId id);
    List<Notification> findByReferenceId(String referenceId);
    List<Notification> findByStatus(NotificationStatus status);
}
