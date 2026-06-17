package com.logistics.warehouse.domain.ports.in;

import com.logistics.warehouse.domain.model.WarehouseId;

public interface CreateWarehouseUseCase {
    WarehouseId create(Command command);
    record Command(String name, String location, double maxWeightKg, double maxVolumeM3) {}
}
