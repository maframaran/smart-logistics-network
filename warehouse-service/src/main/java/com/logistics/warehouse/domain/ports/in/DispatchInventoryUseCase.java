package com.logistics.warehouse.domain.ports.in;

import com.logistics.warehouse.domain.model.WarehouseId;

public interface DispatchInventoryUseCase {
    void dispatch(Command command);
    record Command(WarehouseId warehouseId, String sku, int quantity) {}
}
