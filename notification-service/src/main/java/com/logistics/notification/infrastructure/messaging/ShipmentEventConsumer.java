package com.logistics.notification.infrastructure.messaging;

import com.logistics.notification.domain.model.NotificationChannel;
import com.logistics.notification.domain.model.NotificationType;
import com.logistics.notification.domain.ports.in.SendNotificationUseCase;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentAssignedPayload;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentCancelledPayload;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentCreatedPayload;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentDeliveredPayload;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Objects;

// Consumes Avro GenericRecord (KafkaAvroDeserializer), maps to typed DTO records, dispatches notifications.
// ADR-026: typed records at the boundary. ADR-032: Avro on the wire, GenericRecord → DTO in the mapper.
@Component
public class ShipmentEventConsumer {

    private static final String UNKNOWN_ACTOR = "unknown";
    private static final String SHIPMENT_PREFIX = "Shipment ";

    private final SendNotificationUseCase sendNotification;
    private final ShipmentEventAvroMapper avroMapper;

    public ShipmentEventConsumer(SendNotificationUseCase sendNotification, ShipmentEventAvroMapper avroMapper) {
        this.sendNotification = sendNotification;
        this.avroMapper = avroMapper;
    }

    @KafkaListener(topics = "shipment.created", groupId = "notification-service")
    public void onShipmentCreated(
            @Payload GenericRecord record,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        ShipmentCreatedPayload payload = avroMapper.toShipmentCreated(record);
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
            @Payload GenericRecord record,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        ShipmentAssignedPayload payload = avroMapper.toShipmentAssigned(record);
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
            @Payload GenericRecord record,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        ShipmentDeliveredPayload payload = avroMapper.toShipmentDelivered(record);
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
            @Payload GenericRecord record,
            @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        ShipmentCancelledPayload payload = avroMapper.toShipmentCancelled(record);
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
