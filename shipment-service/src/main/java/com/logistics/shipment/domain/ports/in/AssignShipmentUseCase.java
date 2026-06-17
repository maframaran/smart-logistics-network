package com.logistics.shipment.domain.ports.in;

import com.logistics.shipment.domain.model.ShipmentId;

public interface AssignShipmentUseCase {

    void assign(Command command);

    record Command(ShipmentId shipmentId, String vehicleId, String driverId, String routeId) {}
}
