package com.logistics.driver.application.usecases;

import com.logistics.driver.domain.model.Driver;
import com.logistics.driver.domain.ports.in.UpdateDriverStatusUseCase;
import com.logistics.driver.domain.ports.out.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateDriverStatusService implements UpdateDriverStatusUseCase {

    private final DriverRepository repository;

    public UpdateDriverStatusService(DriverRepository repository) {
        this.repository = repository;
    }

    @Override
    public void update(Command command) {
        Driver driver = repository.findById(command.driverId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + command.driverId()));
        driver.updateStatus(command.newStatus(), command.reason());
        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(driver);
    }
}
