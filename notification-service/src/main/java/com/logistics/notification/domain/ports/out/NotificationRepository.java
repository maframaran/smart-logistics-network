package com.logistics.notification.domain.ports.out;

import com.logistics.notification.domain.model.Notification;
import com.logistics.notification.domain.model.NotificationId;
import com.logistics.notification.domain.model.NotificationStatus;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    void save(Notification notification);
    Optional<Notification> findById(NotificationId id);
    List<Notification> findByReferenceId(String referenceId);
    List<Notification> findByStatus(NotificationStatus status);
}
