package com.logistics.shipment.domain.ports.in;

import com.logistics.shipment.domain.model.Address;
import com.logistics.shipment.domain.model.CargoSpec;
import com.logistics.shipment.domain.model.SlaType;

import java.time.Instant;

// Command — carries input data from the inbound adapter to the use case.
// Plain record — no framework annotations.
public record CreateShipmentCommand(
        Address origin,
        Address destination,
        CargoSpec cargoSpec,
        SlaType slaType,
        Instant requiredDeliveryDate
) {}
