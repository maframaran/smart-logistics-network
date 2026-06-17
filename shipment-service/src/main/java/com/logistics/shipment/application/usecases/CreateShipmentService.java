package com.logistics.shipment.application.usecases;

import com.logistics.shipment.domain.model.Shipment;
import com.logistics.shipment.domain.model.ShipmentId;
import com.logistics.shipment.domain.ports.in.CreateShipmentUseCase;
import com.logistics.shipment.domain.ports.out.ShipmentEventPublisher;
import com.logistics.shipment.domain.ports.out.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateShipmentService implements CreateShipmentUseCase {

    private final ShipmentRepository repository;
    private final ShipmentEventPublisher eventPublisher;

    public CreateShipmentService(ShipmentRepository repository, ShipmentEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
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

        repository.save(shipment);

        shipment.pullDomainEvents().forEach(eventPublisher::publish);

        return shipment.getId();
    }
}
