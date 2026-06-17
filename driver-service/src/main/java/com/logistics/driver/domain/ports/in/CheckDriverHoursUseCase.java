package com.logistics.driver.domain.ports.in;

import com.logistics.driver.domain.model.DriverId;

import java.time.Duration;
import java.time.LocalDate;

public interface CheckDriverHoursUseCase {

    // Returns true if the driver can work the additional duration on the given date without exceeding BR-005
    boolean canWork(DriverId driverId, LocalDate date, Duration additionalDuration);
}
