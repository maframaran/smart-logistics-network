package com.logistics.shipment.application.usecases;

import com.logistics.shipment.domain.events.ShipmentCreated;
import com.logistics.shipment.domain.model.Shipment;
import com.logistics.shipment.domain.model.ShipmentId;
import com.logistics.shipment.domain.ports.in.CreateShipmentCommand;
import com.logistics.shipment.domain.ports.in.CreateShipmentUseCase;
import com.logistics.shipment.domain.ports.out.ShipmentEventPublisher;
import com.logistics.shipment.domain.ports.out.ShipmentRepository;

import java.time.Instant;

// Use Case Implementation — spec: specs/features/F-001-create-shipment.md § Workflow
// Implements the inbound port. Calls outbound ports only via interfaces.
// @Service annotation lives here (application layer may use Spring annotations).
// The domain layer (Shipment, CargoSpec) has NO Spring dependency.
public class CreateShipmentService implements CreateShipmentUseCase {

    private final ShipmentRepository repository;
    private final ShipmentEventPublisher eventPublisher;

    public CreateShipmentService(ShipmentRepository repository,
                                 ShipmentEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ShipmentId create(CreateShipmentCommand command) {
        // Workflow step 3 — requiredDeliveryDate must be in the future
        // spec: specs/features/F-001-create-shipment.md § Edge Cases EC-002
        if (command.requiredDeliveryDate().isBefore(Instant.now())) {
            throw new InvalidDeliveryDateException(command.requiredDeliveryDate());
        }

        // Workflow step 4 — CargoSpec constructor enforces BR-001/BR-002
        // Workflow step 5 — create aggregate in CREATED status
        Shipment shipment = Shipment.create(
                command.origin(),
                command.destination(),
                command.cargoSpec(),
                command.slaType(),
                command.requiredDeliveryDate()
        );

        // Workflow step 5 — persist
        repository.save(shipment);

        // Workflow step 6 — publish domain events raised by aggregate
        // spec: specs/features/F-001-create-shipment.md § AC-002
        shipment.pullDomainEvents().forEach(event -> {
            if (event instanceof ShipmentCreated e) {
                eventPublisher.publish(e);
            }
        });

        return shipment.getId();
    }
}
