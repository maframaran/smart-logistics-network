package com.logistics.fleet.application.usecases;

import com.logistics.fleet.domain.model.Vehicle;
import com.logistics.fleet.domain.ports.in.UpdateVehicleStatusUseCase;
import com.logistics.fleet.domain.ports.out.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateVehicleStatusService implements UpdateVehicleStatusUseCase {

    private final VehicleRepository repository;

    public UpdateVehicleStatusService(VehicleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void update(Command command) {
        Vehicle vehicle = repository.findById(command.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + command.vehicleId()));
        vehicle.updateStatus(command.newStatus(), command.reason());
        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(vehicle);
    }
}
