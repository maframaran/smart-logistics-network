package com.logistics.driver.application.usecases;

import com.logistics.driver.domain.model.Driver;
import com.logistics.driver.domain.model.DriverId;
import com.logistics.driver.domain.ports.in.RegisterDriverUseCase;
import com.logistics.driver.domain.ports.out.DriverEventPublisher;
import com.logistics.driver.domain.ports.out.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterDriverService implements RegisterDriverUseCase {

    private final DriverRepository repository;
    private final DriverEventPublisher eventPublisher;

    public RegisterDriverService(DriverRepository repository, DriverEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DriverId register(Command command) {
        Driver driver = Driver.register(command.fullName(), command.licenseNumber(),
                command.licenseClass(), command.hazmaterialCertified(), command.carrierId());
        repository.save(driver);
        driver.pullDomainEvents().forEach(eventPublisher::publish);
        return driver.getId();
    }
}
