package com.logistics.fleet.application.usecases;

import com.logistics.fleet.domain.model.Vehicle;
import com.logistics.fleet.domain.model.VehicleId;
import com.logistics.fleet.domain.ports.in.RegisterVehicleUseCase;
import com.logistics.fleet.domain.ports.out.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterVehicleService implements RegisterVehicleUseCase {

    private final VehicleRepository repository;

    public RegisterVehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    @Override
    public VehicleId register(Command command) {
        Vehicle vehicle = Vehicle.register(command.licensePlate(), command.type(), command.capacity(), command.carrierId());
        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(vehicle);
        return vehicle.getId();
    }
}
