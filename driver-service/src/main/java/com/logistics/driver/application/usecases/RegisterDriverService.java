package com.logistics.driver.application.usecases;

import com.logistics.driver.domain.model.Driver;
import com.logistics.driver.domain.model.DriverId;
import com.logistics.driver.domain.ports.in.RegisterDriverUseCase;
import com.logistics.driver.domain.ports.out.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterDriverService implements RegisterDriverUseCase {

    private final DriverRepository repository;

    public RegisterDriverService(DriverRepository repository) {
        this.repository = repository;
    }

    @Override
    public DriverId register(Command command) {
        Driver driver = Driver.register(command.fullName(), command.licenseNumber(),
                command.licenseClass(), command.hazmaterialCertified(), command.carrierId());
        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(driver);
        return driver.getId();
    }
}
