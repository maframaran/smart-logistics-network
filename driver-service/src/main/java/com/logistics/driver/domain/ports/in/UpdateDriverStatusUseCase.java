package com.logistics.driver.domain.ports.in;

import com.logistics.driver.domain.model.DriverId;
import com.logistics.driver.domain.model.DriverStatus;

public interface UpdateDriverStatusUseCase {

    void update(Command command);

    record Command(DriverId driverId, DriverStatus newStatus, String reason) {}
}
