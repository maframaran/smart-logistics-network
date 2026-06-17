package com.logistics.fleet.application.usecases;

import com.logistics.fleet.domain.model.Vehicle;
import com.logistics.fleet.domain.model.VehicleId;
import com.logistics.fleet.domain.model.VehicleStatus;
import com.logistics.fleet.domain.ports.in.GetVehicleUseCase;
import com.logistics.fleet.domain.ports.out.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetVehicleService implements GetVehicleUseCase {

    private final VehicleRepository repository;

    public GetVehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Vehicle findById(VehicleId id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
    }

    @Override
    public List<Vehicle> findAvailable(double requiredWeightKg, double requiredVolumeM3, boolean needsColdChain, boolean needsHazmat) {
        return repository.findByStatus(VehicleStatus.AVAILABLE).stream()
                .filter(v -> v.canCarry(requiredWeightKg, requiredVolumeM3))
                .filter(v -> !needsColdChain || v.supportsColdChain())
                .filter(v -> !needsHazmat || v.supportsHazmat())
                .toList();
    }

    @Override
    public List<Vehicle> findByStatus(VehicleStatus status) {
        return repository.findByStatus(status);
    }
}
