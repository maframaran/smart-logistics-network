package com.logistics.fleet.application.usecases;

import com.logistics.fleet.domain.model.Vehicle;
import com.logistics.fleet.domain.model.VehicleId;
import com.logistics.fleet.domain.ports.in.RegisterVehicleUseCase;
import com.logistics.fleet.domain.ports.out.VehicleEventPublisher;
import com.logistics.fleet.domain.ports.out.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterVehicleService implements RegisterVehicleUseCase {

    private final VehicleRepository repository;
    private final VehicleEventPublisher eventPublisher;

    public RegisterVehicleService(VehicleRepository repository, VehicleEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public VehicleId register(Command command) {
        Vehicle vehicle = Vehicle.register(command.licensePlate(), command.type(), command.capacity(), command.carrierId());
        repository.save(vehicle);
        vehicle.pullDomainEvents().forEach(eventPublisher::publish);
        return vehicle.getId();
    }
}
