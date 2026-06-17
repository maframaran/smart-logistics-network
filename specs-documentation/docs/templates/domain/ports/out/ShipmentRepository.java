package com.logistics.shipment.domain.ports.out;

import com.logistics.shipment.domain.model.Shipment;
import com.logistics.shipment.domain.model.ShipmentId;
import com.logistics.shipment.domain.model.ShipmentStatus;

import java.util.List;
import java.util.Optional;

// Outbound Port — spec: architecture/domains.md § Shipment Domain § Repository Port
// Implemented by ShipmentJpaRepository (infrastructure/persistence).
// No JPA annotations here — the domain has no persistence dependency.
public interface ShipmentRepository {

    void save(Shipment shipment);

    Optional<Shipment> findById(ShipmentId id);

    List<Shipment> findByStatus(ShipmentStatus status);
}
