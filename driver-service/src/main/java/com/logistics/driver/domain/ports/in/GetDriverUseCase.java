package com.logistics.driver.domain.ports.in;

import com.logistics.driver.domain.model.Driver;
import com.logistics.driver.domain.model.DriverId;
import com.logistics.driver.domain.model.DriverStatus;

import java.util.List;

public interface GetDriverUseCase {

    Driver findById(DriverId id);

    List<Driver> findByStatus(DriverStatus status);

    List<Driver> findAvailableDrivers(boolean requiresHazmat);
}
