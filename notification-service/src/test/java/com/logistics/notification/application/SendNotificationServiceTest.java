package com.logistics.notification.application;

import com.logistics.notification.application.usecases.SendNotificationService;
import com.logistics.notification.domain.model.*;
import com.logistics.notification.domain.ports.in.SendNotificationUseCase;
import com.logistics.notification.domain.ports.out.NotificationRepository;
import com.logistics.notification.domain.ports.out.NotificationSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {

    @Mock NotificationRepository repository;
    @Mock NotificationSender emailSender;

    @Test
    void send_callsSenderAndSavesNotification() {
        when(emailSender.supports(NotificationChannel.EMAIL)).thenReturn(true);
        SendNotificationService service = new SendNotificationService(repository, List.of(emailSender));

        SendNotificationUseCase.Command command = new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_CREATED, NotificationChannel.EMAIL,
                "user@example.com", "User", "Subject", "Body text", "ship-1"
        );

        NotificationId id = service.send(command);

        assertThat(id).isNotNull();
        verify(emailSender).send(any(Notification.class));
        verify(repository, times(2)).save(any(Notification.class)); // once pending, once sent
    }

    @Test
    void send_senderThrows_marksFailedAndSaves() {
        when(emailSender.supports(NotificationChannel.EMAIL)).thenReturn(true);
        doThrow(new RuntimeException("SMTP error")).when(emailSender).send(any());
        SendNotificationService service = new SendNotificationService(repository, List.of(emailSender));

        SendNotificationUseCase.Command command = new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_DELIVERED, NotificationChannel.EMAIL,
                "user@example.com", "User", "Subject", "Body", "ship-2"
        );

        NotificationId id = service.send(command);

        assertThat(id).isNotNull();
        // saved twice: initial PENDING, then FAILED
        verify(repository, times(2)).save(any(Notification.class));
    }

    @Test
    void send_noSenderForChannel_throws() {
        when(emailSender.supports(NotificationChannel.EMAIL)).thenReturn(false);
        SendNotificationService service = new SendNotificationService(repository, List.of(emailSender));

        SendNotificationUseCase.Command command = new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_CANCELLED, NotificationChannel.EMAIL,
                "user@example.com", "User", "Subject", "Body", "ship-3"
        );

        assertThatThrownBy(() -> service.send(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No sender for channel");
    }
}
