package com.logistics.driver.domain.ports.in;

import com.logistics.driver.domain.model.DriverId;
import com.logistics.driver.domain.model.LicenseClass;

public interface RegisterDriverUseCase {

    DriverId register(Command command);

    record Command(String fullName, String licenseNumber, LicenseClass licenseClass,
                   boolean hazmaterialCertified, String carrierId) {}
}
