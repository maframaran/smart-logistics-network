package com.logistics.notification.infrastructure.messaging;

import com.logistics.notification.domain.model.NotificationChannel;
import com.logistics.notification.domain.model.NotificationType;
import com.logistics.notification.domain.ports.in.SendNotificationUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

// Consumes shipment domain events and dispatches notifications.
// Payload arrives as a deserialized Map via Spring's JsonDeserializer.
@Component
public class ShipmentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentEventConsumer.class);

    private final SendNotificationUseCase sendNotification;

    public ShipmentEventConsumer(SendNotificationUseCase sendNotification) {
        this.sendNotification = sendNotification;
    }

    @KafkaListener(topics = "shipment.created", groupId = "notification-service")
    public void onShipmentCreated(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        String shipperId = (String) payload.getOrDefault("shipperId", "unknown");
        sendNotification.send(new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_CREATED, NotificationChannel.EMAIL,
                resolveEmail(shipperId), "Shipper",
                "Your shipment has been created",
                "Shipment " + shipmentId + " has been successfully created and is awaiting scheduling.",
                shipmentId));
    }

    @KafkaListener(topics = "shipment.assigned", groupId = "notification-service")
    public void onShipmentAssigned(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        String driverId = (String) payload.getOrDefault("driverId", "unknown");
        sendNotification.send(new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_ASSIGNED, NotificationChannel.EMAIL,
                resolveEmail(driverId), "Driver",
                "New pickup assignment",
                "You have been assigned to shipment " + shipmentId + ". Please check your schedule.",
                shipmentId));
    }

    @KafkaListener(topics = "shipment.delivered", groupId = "notification-service")
    public void onShipmentDelivered(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        String shipperId = (String) payload.getOrDefault("shipperId", "unknown");
        sendNotification.send(new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_DELIVERED, NotificationChannel.EMAIL,
                resolveEmail(shipperId), "Customer",
                "Your shipment has been delivered",
                "Shipment " + shipmentId + " was successfully delivered. Thank you for using our platform.",
                shipmentId));
    }

    @KafkaListener(topics = "shipment.cancelled", groupId = "notification-service")
    public void onShipmentCancelled(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        String reason = (String) payload.getOrDefault("reason", "not specified");
        sendNotification.send(new SendNotificationUseCase.Command(
                NotificationType.SHIPMENT_CANCELLED, NotificationChannel.EMAIL,
                "shipper@platform.local", "Shipper",
                "Shipment cancelled",
                "Shipment " + shipmentId + " has been cancelled. Reason: " + reason,
                shipmentId));
    }

    // In Phase 4 resolved via a contact/profile service call
    private String resolveEmail(String actorId) {
        return actorId + "@platform.local";
    }
}
