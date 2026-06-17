package com.logistics.driver.application.usecases;

import com.logistics.driver.domain.model.Driver;
import com.logistics.driver.domain.model.DriverId;
import com.logistics.driver.domain.model.DriverStatus;
import com.logistics.driver.domain.ports.in.GetDriverUseCase;
import com.logistics.driver.domain.ports.out.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetDriverService implements GetDriverUseCase {

    private final DriverRepository repository;

    public GetDriverService(DriverRepository repository) {
        this.repository = repository;
    }

    @Override
    public Driver findById(DriverId id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + id));
    }

    @Override
    public List<Driver> findByStatus(DriverStatus status) {
        return repository.findByStatus(status);
    }

    @Override
    public List<Driver> findAvailableDrivers(boolean requiresHazmat) {
        return repository.findByStatus(DriverStatus.AVAILABLE).stream()
                .filter(d -> !requiresHazmat || d.canDriveHazmat())
                .toList();
    }
}
