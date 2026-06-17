package com.logistics.driver.domain.ports.out;

import com.logistics.driver.domain.model.Driver;
import com.logistics.driver.domain.model.DriverId;
import com.logistics.driver.domain.model.DriverStatus;

import java.util.List;
import java.util.Optional;

public interface DriverRepository {

    void save(Driver driver);

    Optional<Driver> findById(DriverId id);

    List<Driver> findByStatus(DriverStatus status);
}
