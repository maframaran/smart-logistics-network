package com.logistics.driver.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.driver.domain.events.DriverRegistered;
import com.logistics.driver.domain.events.DriverStatusChanged;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

@Component
class DriverEventAvroMapper {

    SpecificRecord toAvro(DomainEvent event) {
        return switch (event) {
            case DriverRegistered e    -> toAvro(e);
            case DriverStatusChanged e -> toAvro(e);
            default -> throw new IllegalArgumentException("Unknown event: " + event.getClass().getSimpleName());
        };
    }

    private com.logistics.driver.avro.DriverRegistered toAvro(DriverRegistered e) {
        return com.logistics.driver.avro.DriverRegistered.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setFullName(e.fullName())
                .setLicenseNumber(e.licenseNumber())
                .setLicenseClass(e.licenseClass().name())
                .setHazmaterialCertified(e.hazmaterialCertified())
                .setCarrierId(e.carrierId())
                .build();
    }

    private com.logistics.driver.avro.DriverStatusChanged toAvro(DriverStatusChanged e) {
        return com.logistics.driver.avro.DriverStatusChanged.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setPreviousStatus(e.previousStatus().name())
                .setNewStatus(e.newStatus().name())
                .setReason(e.reason())
                .build();
    }
}
