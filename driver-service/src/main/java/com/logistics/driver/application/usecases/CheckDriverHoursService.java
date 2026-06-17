package com.logistics.driver.application.usecases;

import com.logistics.driver.domain.model.Driver;
import com.logistics.driver.domain.model.DriverId;
import com.logistics.driver.domain.ports.in.CheckDriverHoursUseCase;
import com.logistics.driver.domain.ports.out.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class CheckDriverHoursService implements CheckDriverHoursUseCase {

    private final DriverRepository repository;

    public CheckDriverHoursService(DriverRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean canWork(DriverId driverId, LocalDate date, Duration additionalDuration) {
        Driver driver = repository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
        return !driver.getDrivingSessionFor(date).wouldExceedLimit(additionalDuration);
    }
}
