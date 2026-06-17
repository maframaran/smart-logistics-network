package com.logistics.fleet.application.usecases;

import com.logistics.fleet.domain.model.Vehicle;
import com.logistics.fleet.domain.ports.in.UpdateVehicleStatusUseCase;
import com.logistics.fleet.domain.ports.out.VehicleEventPublisher;
import com.logistics.fleet.domain.ports.out.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateVehicleStatusService implements UpdateVehicleStatusUseCase {

    private final VehicleRepository repository;
    private final VehicleEventPublisher eventPublisher;

    public UpdateVehicleStatusService(VehicleRepository repository, VehicleEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void update(Command command) {
        Vehicle vehicle = repository.findById(command.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + command.vehicleId()));
        vehicle.updateStatus(command.newStatus(), command.reason());
        repository.save(vehicle);
        vehicle.pullDomainEvents().forEach(eventPublisher::publish);
    }
}
