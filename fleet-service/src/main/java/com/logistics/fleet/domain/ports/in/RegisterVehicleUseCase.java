package com.logistics.fleet.domain.ports.in;

import com.logistics.fleet.domain.model.Capacity;
import com.logistics.fleet.domain.model.VehicleId;
import com.logistics.fleet.domain.model.VehicleType;

public interface RegisterVehicleUseCase {

    VehicleId register(Command command);

    record Command(String licensePlate, VehicleType type, Capacity capacity, String carrierId) {}
}
