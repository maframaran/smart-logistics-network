package com.logistics.notification.infrastructure.messaging;

import com.logistics.notification.infrastructure.messaging.dto.ShipmentAssignedPayload;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentCancelledPayload;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentCreatedPayload;
import com.logistics.notification.infrastructure.messaging.dto.ShipmentDeliveredPayload;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;

// Maps Avro GenericRecord (from KafkaAvroDeserializer) to local typed DTO records (ADR-026, ADR-032).
@Component
class ShipmentEventAvroMapper {

    ShipmentCreatedPayload toShipmentCreated(GenericRecord record) {
        return new ShipmentCreatedPayload(str(record, "shipperId"));
    }

    ShipmentAssignedPayload toShipmentAssigned(GenericRecord record) {
        return new ShipmentAssignedPayload(str(record, "driverId"));
    }

    ShipmentDeliveredPayload toShipmentDelivered(GenericRecord record) {
        return new ShipmentDeliveredPayload(str(record, "shipperId"));
    }

    ShipmentCancelledPayload toShipmentCancelled(GenericRecord record) {
        return new ShipmentCancelledPayload(str(record, "reason"));
    }

    private static String str(GenericRecord record, String field) {
        Object value = record.get(field);
        return value == null ? null : value.toString();
    }
}
