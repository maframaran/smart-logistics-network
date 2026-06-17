package com.logistics.notification.application.usecases;

import com.logistics.notification.domain.model.Notification;
import com.logistics.notification.domain.model.NotificationId;
import com.logistics.notification.domain.ports.in.SendNotificationUseCase;
import com.logistics.notification.domain.ports.out.NotificationRepository;
import com.logistics.notification.domain.ports.out.NotificationSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SendNotificationService implements SendNotificationUseCase {

    private final NotificationRepository repository;
    private final List<NotificationSender> senders;

    public SendNotificationService(NotificationRepository repository, List<NotificationSender> senders) {
        this.repository = repository;
        this.senders = senders;
    }

    @Override
    public NotificationId send(Command command) {
        Notification notification = Notification.create(
                command.type(), command.channel(),
                command.recipientAddress(), command.recipientName(),
                command.subject(), command.body(), command.referenceId());

        repository.save(notification);

        NotificationSender sender = senders.stream()
                .filter(s -> s.supports(command.channel()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No sender for channel: " + command.channel()));

        try {
            sender.send(notification);
            notification.markSent();
        } catch (Exception ex) {
            notification.markFailed(ex.getMessage());
        }

        // Save updated status after delivery attempt
        repository.save(notification);
        notification.pullDomainEvents(); // discard internal events (no external publishing needed here)

        return notification.getId();
    }
}
