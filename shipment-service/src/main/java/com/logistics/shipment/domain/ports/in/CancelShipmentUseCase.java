package com.logistics.shipment.domain.ports.in;

import com.logistics.shipment.domain.model.ShipmentId;

public interface CancelShipmentUseCase {

    void cancel(Command command);

    record Command(ShipmentId shipmentId, String reason) {}
}
