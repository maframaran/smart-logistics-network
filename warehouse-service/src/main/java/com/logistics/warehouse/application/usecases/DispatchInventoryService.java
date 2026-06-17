package com.logistics.warehouse.application.usecases;

import com.logistics.warehouse.domain.model.Sku;
import com.logistics.warehouse.domain.model.Warehouse;
import com.logistics.warehouse.domain.ports.in.DispatchInventoryUseCase;
import com.logistics.warehouse.domain.ports.out.WarehouseEventPublisher;
import com.logistics.warehouse.domain.ports.out.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DispatchInventoryService implements DispatchInventoryUseCase {

    private final WarehouseRepository repository;
    private final WarehouseEventPublisher eventPublisher;

    public DispatchInventoryService(WarehouseRepository repository, WarehouseEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void dispatch(Command command) {
        Warehouse warehouse = repository.findById(command.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + command.warehouseId()));

        warehouse.dispatchInventory(new Sku(command.sku()), command.quantity());
        repository.save(warehouse);
        warehouse.pullDomainEvents().forEach(eventPublisher::publish);
    }
}
