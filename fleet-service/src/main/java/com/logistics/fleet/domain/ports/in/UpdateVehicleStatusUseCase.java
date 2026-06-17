package com.logistics.fleet.domain.ports.in;

import com.logistics.fleet.domain.model.VehicleId;
import com.logistics.fleet.domain.model.VehicleStatus;

public interface UpdateVehicleStatusUseCase {

    void update(Command command);

    record Command(VehicleId vehicleId, VehicleStatus newStatus, String reason) {}
}
