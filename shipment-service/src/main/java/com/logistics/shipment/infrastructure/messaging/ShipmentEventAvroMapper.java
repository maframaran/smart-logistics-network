package com.logistics.shipment.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.shipment.domain.events.ShipmentAssigned;
import com.logistics.shipment.domain.events.ShipmentCancelled;
import com.logistics.shipment.domain.events.ShipmentCreated;
import com.logistics.shipment.domain.model.Address;
import com.logistics.shipment.domain.model.CargoSpec;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

@Component
class ShipmentEventAvroMapper {

    SpecificRecord toAvro(DomainEvent event) {
        return switch (event) {
            case ShipmentCreated e   -> toAvro(e);
            case ShipmentAssigned e  -> toAvro(e);
            case ShipmentCancelled e -> toAvro(e);
            default -> throw new IllegalArgumentException("Unknown event: " + event.getClass().getSimpleName());
        };
    }

    private com.logistics.shipment.avro.ShipmentCreated toAvro(ShipmentCreated e) {
        return com.logistics.shipment.avro.ShipmentCreated.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setShipperId(e.shipperId())
                .setOrigin(toAvro(e.origin()))
                .setDestination(toAvro(e.destination()))
                .setCargoSpec(toAvro(e.cargoSpec()))
                .setSlaType(e.slaType().name())
                .setRequiredDeliveryDate((int) e.requiredDeliveryDate().toEpochDay())
                .build();
    }

    private com.logistics.shipment.avro.Address toAvro(Address a) {
        return com.logistics.shipment.avro.Address.newBuilder()
                .setStreet(a.street())
                .setCity(a.city())
                .setState(a.state())
                .setPostalCode(a.postalCode())
                .setCountry(a.country())
                .setLatitude(a.latitude())
                .setLongitude(a.longitude())
                .build();
    }

    private com.logistics.shipment.avro.CargoSpec toAvro(CargoSpec c) {
        return com.logistics.shipment.avro.CargoSpec.newBuilder()
                .setWeightKg(c.weightKg())
                .setVolumeM3(c.volumeM3())
                .setRequiresHazmat(c.requiresHazmat())
                .setRequiresColdChain(c.requiresColdChain())
                .build();
    }

    private com.logistics.shipment.avro.ShipmentAssigned toAvro(ShipmentAssigned e) {
        return com.logistics.shipment.avro.ShipmentAssigned.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setVehicleId(e.vehicleId())
                .setDriverId(e.driverId())
                .setRouteId(e.routeId())
                .build();
    }

    private com.logistics.shipment.avro.ShipmentCancelled toAvro(ShipmentCancelled e) {
        return com.logistics.shipment.avro.ShipmentCancelled.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setReason(e.reason())
                .setCancellationFeeApplied(e.cancellationFeeApplied())
                .build();
    }
}
