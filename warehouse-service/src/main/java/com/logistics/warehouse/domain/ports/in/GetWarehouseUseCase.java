package com.logistics.warehouse.domain.ports.in;

import com.logistics.warehouse.domain.model.Warehouse;
import com.logistics.warehouse.domain.model.WarehouseId;

import java.util.List;

public interface GetWarehouseUseCase {
    Warehouse findById(WarehouseId id);
    List<Warehouse> findAll();
}
