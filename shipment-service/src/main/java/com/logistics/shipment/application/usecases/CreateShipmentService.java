package com.logistics.shipment.application.usecases;

import com.logistics.shipment.domain.model.Shipment;
import com.logistics.shipment.domain.model.ShipmentId;
import com.logistics.shipment.domain.ports.in.CreateShipmentUseCase;
import com.logistics.shipment.domain.ports.out.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateShipmentService implements CreateShipmentUseCase {

    private final ShipmentRepository repository;

    public CreateShipmentService(ShipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public ShipmentId create(Command command) {
        Shipment shipment = Shipment.create(
                command.shipperId(),
                command.origin(),
                command.destination(),
                command.cargoSpec(),
                command.slaType(),
                command.requiredDeliveryDate()
        );

        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(shipment);

        return shipment.getId();
    }
}
