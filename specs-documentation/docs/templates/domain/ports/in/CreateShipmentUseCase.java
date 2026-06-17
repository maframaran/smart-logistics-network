package com.logistics.shipment.domain.ports.in;

import com.logistics.shipment.domain.model.ShipmentId;

// Inbound Port — spec: specs/features/F-001-create-shipment.md
// Implemented by CreateShipmentService (application layer).
// Called by ShipmentController (infrastructure/rest).
// No framework annotations here — this interface belongs to the domain.
public interface CreateShipmentUseCase {

    ShipmentId create(CreateShipmentCommand command);
}
