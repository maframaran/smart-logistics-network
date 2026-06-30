package com.logistics.warehouse.application.usecases;

import com.logistics.warehouse.domain.model.*;
import com.logistics.warehouse.domain.ports.in.ReceiveInventoryUseCase;
import com.logistics.warehouse.domain.ports.out.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReceiveInventoryService implements ReceiveInventoryUseCase {

    private final WarehouseRepository repository;

    public ReceiveInventoryService(WarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void receive(Command command) {
        Warehouse warehouse = repository.findById(command.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + command.warehouseId()));

        InventoryItem item = new InventoryItem(
                InventoryItemId.generate(),
                new Sku(command.sku()),
                command.description(),
                command.weightKg(),
                command.volumeM3(),
                command.quantity()
        );

        warehouse.receiveInventory(item);
        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(warehouse);
    }
}
