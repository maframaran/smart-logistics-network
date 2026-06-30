package com.logistics.warehouse.application.usecases;

import com.logistics.warehouse.domain.model.Sku;
import com.logistics.warehouse.domain.model.Warehouse;
import com.logistics.warehouse.domain.ports.in.DispatchInventoryUseCase;
import com.logistics.warehouse.domain.ports.out.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DispatchInventoryService implements DispatchInventoryUseCase {

    private final WarehouseRepository repository;

    public DispatchInventoryService(WarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void dispatch(Command command) {
        Warehouse warehouse = repository.findById(command.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + command.warehouseId()));

        warehouse.dispatchInventory(new Sku(command.sku()), command.quantity());
        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(warehouse);
    }
}
