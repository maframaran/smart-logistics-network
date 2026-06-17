package com.logistics.warehouse.application.usecases;

import com.logistics.warehouse.domain.model.Warehouse;
import com.logistics.warehouse.domain.model.WarehouseId;
import com.logistics.warehouse.domain.ports.in.CreateWarehouseUseCase;
import com.logistics.warehouse.domain.ports.out.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateWarehouseService implements CreateWarehouseUseCase {

    private final WarehouseRepository repository;

    public CreateWarehouseService(WarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public WarehouseId create(Command command) {
        Warehouse warehouse = Warehouse.create(command.name(), command.location(), command.maxWeightKg(), command.maxVolumeM3());
        repository.save(warehouse);
        return warehouse.getId();
    }
}
