package com.logistics.shipment.domain.ports.in;

import com.logistics.shipment.domain.model.Shipment;
import com.logistics.shipment.domain.model.ShipmentId;
import com.logistics.shipment.domain.model.ShipmentStatus;

import java.util.List;

public interface GetShipmentUseCase {

    Shipment findById(ShipmentId id);

    List<Shipment> findByStatus(ShipmentStatus status);
}
