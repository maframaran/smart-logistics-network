package com.logistics.warehouse.application.usecases;

import com.logistics.warehouse.domain.model.Warehouse;
import com.logistics.warehouse.domain.model.WarehouseId;
import com.logistics.warehouse.domain.ports.in.GetWarehouseUseCase;
import com.logistics.warehouse.domain.ports.out.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetWarehouseService implements GetWarehouseUseCase {

    private final WarehouseRepository repository;

    public GetWarehouseService(WarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Warehouse findById(WarehouseId id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + id));
    }

    @Override
    public List<Warehouse> findAll() {
        return repository.findAll();
    }
}
