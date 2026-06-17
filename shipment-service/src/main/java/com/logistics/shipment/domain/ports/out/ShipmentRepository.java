package com.logistics.shipment.domain.ports.out;

import com.logistics.shipment.domain.model.Shipment;
import com.logistics.shipment.domain.model.ShipmentId;
import com.logistics.shipment.domain.model.ShipmentStatus;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository {

    void save(Shipment shipment);

    Optional<Shipment> findById(ShipmentId id);

    List<Shipment> findByStatus(ShipmentStatus status);
}
