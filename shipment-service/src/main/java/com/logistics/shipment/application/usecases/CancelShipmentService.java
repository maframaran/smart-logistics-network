package com.logistics.shipment.application.usecases;

import com.logistics.shipment.domain.model.Shipment;
import com.logistics.shipment.domain.ports.in.CancelShipmentUseCase;
import com.logistics.shipment.domain.ports.out.ShipmentEventPublisher;
import com.logistics.shipment.domain.ports.out.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CancelShipmentService implements CancelShipmentUseCase {

    private final ShipmentRepository repository;
    private final ShipmentEventPublisher eventPublisher;

    public CancelShipmentService(ShipmentRepository repository, ShipmentEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void cancel(Command command) {
        Shipment shipment = repository.findById(command.shipmentId())
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + command.shipmentId()));

        shipment.cancel(command.reason());

        repository.save(shipment);

        shipment.pullDomainEvents().forEach(eventPublisher::publish);
    }
}
