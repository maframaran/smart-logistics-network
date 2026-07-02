package com.logistics.fleet.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.fleet.domain.events.VehicleRegistered;
import com.logistics.fleet.domain.events.VehicleStatusChanged;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

@Component
class FleetEventAvroMapper {

    SpecificRecord toAvro(DomainEvent event) {
        return switch (event) {
            case VehicleRegistered e    -> toAvro(e);
            case VehicleStatusChanged e -> toAvro(e);
            default -> throw new IllegalArgumentException("Unknown event: " + event.getClass().getSimpleName());
        };
    }

    private com.logistics.fleet.avro.VehicleRegistered toAvro(VehicleRegistered e) {
        return com.logistics.fleet.avro.VehicleRegistered.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setLicensePlate(e.licensePlate())
                .setVehicleType(e.vehicleType().name())
                .setMaxWeightKg(e.capacity().maxWeightKg())
                .setMaxVolumeM3(e.capacity().maxVolumeM3())
                .setCarrierId(e.carrierId())
                .build();
    }

    private com.logistics.fleet.avro.VehicleStatusChanged toAvro(VehicleStatusChanged e) {
        return com.logistics.fleet.avro.VehicleStatusChanged.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setPreviousStatus(e.previousStatus().name())
                .setNewStatus(e.newStatus().name())
                .setReason(e.reason())
                .build();
    }
}
