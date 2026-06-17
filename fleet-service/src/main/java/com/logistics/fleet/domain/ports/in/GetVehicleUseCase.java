package com.logistics.fleet.domain.ports.in;

import com.logistics.fleet.domain.model.Vehicle;
import com.logistics.fleet.domain.model.VehicleId;
import com.logistics.fleet.domain.model.VehicleStatus;

import java.util.List;

public interface GetVehicleUseCase {

    Vehicle findById(VehicleId id);

    List<Vehicle> findAvailable(double requiredWeightKg, double requiredVolumeM3, boolean needsColdChain, boolean needsHazmat);

    List<Vehicle> findByStatus(VehicleStatus status);
}
