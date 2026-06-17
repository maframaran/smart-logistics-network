package com.logistics.warehouse.application.usecases;

import com.logistics.warehouse.domain.model.*;
import com.logistics.warehouse.domain.ports.in.ReceiveInventoryUseCase;
import com.logistics.warehouse.domain.ports.out.WarehouseEventPublisher;
import com.logistics.warehouse.domain.ports.out.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReceiveInventoryService implements ReceiveInventoryUseCase {

    private final WarehouseRepository repository;
    private final WarehouseEventPublisher eventPublisher;

    public ReceiveInventoryService(WarehouseRepository repository, WarehouseEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
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
        repository.save(warehouse);
        warehouse.pullDomainEvents().forEach(eventPublisher::publish);
    }
}
