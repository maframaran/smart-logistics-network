package com.logistics.notification.infrastructure.messaging;

import com.logistics.notification.domain.model.NotificationChannel;
import com.logistics.notification.domain.model.NotificationType;
import com.logistics.notification.domain.ports.in.SendNotificationUseCase;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentAssignedPayload;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentCancelledPayload;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentCreatedPayload;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentDeliveredPayload;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Objects;

// Consumes shipment domain events and dispatches notifications.
// Payload arrives as a typed record via Spring's JsonDeserializer (type inferred from the listener's @Payload parameter).
@Component
public class ShipmentEventConsumer {

    private static final String UNKNOWN_ACTOR = "unknown";
    private static final String SHIPMENT_PREFIX = "Shipment ";

    private final SendNotificationUseCase sendNotification;

    public ShipmentEventConsumer(SendNotificationUseCase sendNotification) {
        this.sendNotification = sendNotification;
    }

    @KafkaListener(topics = "shipment.created", groupId = "notification-service")
    public void onShipmentCreated(
            @Payload ShipmentCreatedPayload payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        String shipperId = Objects.requireNonNullElse(payload.shipperId(), UNKNOWN_ACTOR);
        sendNotification.send(new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_CREATED, NotificationChannel.EMAIL,
                resolveEmail(shipperId), "Shipper",
                "Your shipment has been created",
                SHIPMENT_PREFIX + shipmentId + " has been successfully created and is awaiting scheduling.",
                shipmentId));
    }

    @KafkaListener(topics = "shipment.assigned", groupId = "notification-service")
    public void onShipmentAssigned(
            @Payload ShipmentAssignedPayload payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        String driverId = Objects.requireNonNullElse(payload.driverId(), UNKNOWN_ACTOR);
        sendNotification.send(new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_ASSIGNED, NotificationChannel.EMAIL,
                resolveEmail(driverId), "Driver",
                "New pickup assignment",
                "You have been assigned to shipment " + shipmentId + ". Please check your schedule.",
                shipmentId));
    }

    @KafkaListener(topics = "shipment.delivered", groupId = "notification-service")
    public void onShipmentDelivered(
            @Payload ShipmentDeliveredPayload payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        String shipperId = Objects.requireNonNullElse(payload.shipperId(), UNKNOWN_ACTOR);
        sendNotification.send(new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_DELIVERED, NotificationChannel.EMAIL,
                resolveEmail(shipperId), "Customer",
                "Your shipment has been delivered",
                SHIPMENT_PREFIX + shipmentId + " was successfully delivered. Thank you for using our platform.",
                shipmentId));
    }

    @KafkaListener(topics = "shipment.cancelled", groupId = "notification-service")
    public void onShipmentCancelled(
            @Payload ShipmentCancelledPayload payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        String reason = Objects.requireNonNullElse(payload.reason(), "not specified");
        sendNotification.send(new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_CANCELLED, NotificationChannel.EMAIL,
                "shipper@platform.local", "Shipper",
                "Shipment cancelled",
                SHIPMENT_PREFIX + shipmentId + " has been cancelled. Reason: " + reason,
                shipmentId));
    }

    // In Phase 4 resolved via a contact/profile service call
    private String resolveEmail(String actorId) {
        return actorId + "@platform.local";
    }
}
