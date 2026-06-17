package com.logistics.fleet.infrastructure.persistence;

import com.logistics.fleet.domain.model.*;
import com.logistics.fleet.domain.ports.out.VehicleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VehicleJpaRepository implements VehicleRepository {

    private final VehicleJpaRepositoryPort jpa;

    public VehicleJpaRepository(VehicleJpaRepositoryPort jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Vehicle v) {
        jpa.save(toEntity(v));
    }

    @Override
    public Optional<Vehicle> findById(VehicleId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Vehicle> findByStatus(VehicleStatus status) {
        return jpa.findByStatus(status.name()).stream().map(this::toDomain).toList();
    }

    private VehicleJpaEntity toEntity(Vehicle v) {
        VehicleJpaEntity e = new VehicleJpaEntity();
        e.id = v.getId().value();
        e.licensePlate = v.getLicensePlate();
        e.type = v.getType().name();
        e.maxWeightKg = v.getCapacity().maxWeightKg();
        e.maxVolumeM3 = v.getCapacity().maxVolumeM3();
        e.carrierId = v.getCarrierId();
        e.status = v.getStatus().name();
        return e;
    }

    private Vehicle toDomain(VehicleJpaEntity e) {
        return Vehicle.reconstitute(
                new VehicleId(e.id),
                e.licensePlate,
                VehicleType.valueOf(e.type),
                new Capacity(e.maxWeightKg, e.maxVolumeM3),
                e.carrierId,
                VehicleStatus.valueOf(e.status)
        );
    }
}
