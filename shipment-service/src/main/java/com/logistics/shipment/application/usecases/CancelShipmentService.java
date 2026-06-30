package com.logistics.shipment.application.usecases;

import com.logistics.shipment.domain.model.Shipment;
import com.logistics.shipment.domain.ports.in.CancelShipmentUseCase;
import com.logistics.shipment.domain.ports.out.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CancelShipmentService implements CancelShipmentUseCase {

    private final ShipmentRepository repository;

    public CancelShipmentService(ShipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void cancel(Command command) {
        Shipment shipment = repository.findById(command.shipmentId())
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + command.shipmentId()));

        shipment.cancel(command.reason());

        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(shipment);
    }
}
