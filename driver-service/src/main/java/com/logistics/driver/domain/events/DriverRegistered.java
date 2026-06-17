package com.logistics.driver.domain.events;

import com.logistics.common.domain.DomainEvent;
import com.logistics.driver.domain.model.LicenseClass;

import java.time.Instant;
import java.util.UUID;

public record DriverRegistered(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        String fullName,
        String licenseNumber,
        LicenseClass licenseClass,
        boolean hazmaterialCertified,
        String carrierId
) implements DomainEvent {

    public static DriverRegistered of(String driverId, String fullName, String licenseNumber,
                                      LicenseClass licenseClass, boolean hazmaterialCertified, String carrierId) {
        return new DriverRegistered(UUID.randomUUID(), Instant.now(), driverId,
                fullName, licenseNumber, licenseClass, hazmaterialCertified, carrierId);
    }
}
