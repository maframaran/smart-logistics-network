package com.logistics.warehouse.domain.ports.in;

import com.logistics.warehouse.domain.model.WarehouseId;

public interface ReceiveInventoryUseCase {
    void receive(Command command);

    record Command(
            WarehouseId warehouseId,
            String sku,
            String description,
            double weightKg,
            double volumeM3,
            int quantity
    ) {}
}
