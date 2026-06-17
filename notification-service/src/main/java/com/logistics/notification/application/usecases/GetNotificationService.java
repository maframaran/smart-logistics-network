package com.logistics.notification.application.usecases;

import com.logistics.notification.domain.model.Notification;
import com.logistics.notification.domain.model.NotificationId;
import com.logistics.notification.domain.model.NotificationStatus;
import com.logistics.notification.domain.ports.in.GetNotificationUseCase;
import com.logistics.notification.domain.ports.out.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetNotificationService implements GetNotificationUseCase {

    private final NotificationRepository repository;

    public GetNotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notification findById(NotificationId id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
    }

    @Override
    public List<Notification> findByReferenceId(String referenceId) {
        return repository.findByReferenceId(referenceId);
    }

    @Override
    public List<Notification> findByStatus(NotificationStatus status) {
        return repository.findByStatus(status);
    }
}
