package com.logistics.fleet.domain.ports.out;

import com.logistics.fleet.domain.model.Vehicle;
import com.logistics.fleet.domain.model.VehicleId;
import com.logistics.fleet.domain.model.VehicleStatus;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository {

    void save(Vehicle vehicle);

    Optional<Vehicle> findById(VehicleId id);

    List<Vehicle> findByStatus(VehicleStatus status);
}
