package com.logistics.shipment.domain.events;

import com.logistics.common.domain.DomainEvent;
import com.logistics.shipment.domain.model.Address;
import com.logistics.shipment.domain.model.CargoSpec;
import com.logistics.shipment.domain.model.SlaType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ShipmentCreated(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        String shipperId,
        Address origin,
        Address destination,
        CargoSpec cargoSpec,
        SlaType slaType,
        LocalDate requiredDeliveryDate
) implements DomainEvent {

    public static ShipmentCreated of(
            String shipmentId,
            String shipperId,
            Address origin,
            Address destination,
            CargoSpec cargoSpec,
            SlaType slaType,
            LocalDate requiredDeliveryDate
    ) {
        return new ShipmentCreated(
                UUID.randomUUID(),
                Instant.now(),
                shipmentId,
                shipperId,
                origin,
                destination,
                cargoSpec,
                slaType,
                requiredDeliveryDate
        );
    }
}
