package com.logistics.warehouse.domain.ports.out;

import com.logistics.warehouse.domain.model.Warehouse;
import com.logistics.warehouse.domain.model.WarehouseId;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository {
    void save(Warehouse warehouse);
    Optional<Warehouse> findById(WarehouseId id);
    List<Warehouse> findAll();
}
