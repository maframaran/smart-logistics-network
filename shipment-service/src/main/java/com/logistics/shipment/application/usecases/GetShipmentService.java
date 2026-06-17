package com.logistics.shipment.application.usecases;

import com.logistics.shipment.domain.model.Shipment;
import com.logistics.shipment.domain.model.ShipmentId;
import com.logistics.shipment.domain.model.ShipmentStatus;
import com.logistics.shipment.domain.ports.in.GetShipmentUseCase;
import com.logistics.shipment.domain.ports.out.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetShipmentService implements GetShipmentUseCase {

    private final ShipmentRepository repository;

    public GetShipmentService(ShipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Shipment findById(ShipmentId id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + id));
    }

    @Override
    public List<Shipment> findByStatus(ShipmentStatus status) {
        return repository.findByStatus(status);
    }
}
