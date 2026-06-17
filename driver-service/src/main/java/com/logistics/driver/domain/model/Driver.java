package com.logistics.driver.domain.model;

import com.logistics.common.domain.AggregateRoot;
import com.logistics.driver.domain.events.DriverRegistered;
import com.logistics.driver.domain.events.DriverStatusChanged;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Driver extends AggregateRoot {

    private final DriverId id;
    private final String fullName;
    private final String licenseNumber;
    private final LicenseClass licenseClass;
    private final boolean hazmaterialCertified;
    private final String carrierId;
    private DriverStatus status;

    // BR-005: driving hours per day — key is calendar date
    private final Map<LocalDate, DrivingSession> drivingSessions;

    private Driver(DriverId id, String fullName, String licenseNumber, LicenseClass licenseClass,
                   boolean hazmaterialCertified, String carrierId, DriverStatus status,
                   Map<LocalDate, DrivingSession> drivingSessions) {
        this.id = id;
        this.fullName = fullName;
        this.licenseNumber = licenseNumber;
        this.licenseClass = licenseClass;
        this.hazmaterialCertified = hazmaterialCertified;
        this.carrierId = carrierId;
        this.status = status;
        this.drivingSessions = new HashMap<>(drivingSessions);
    }

    public static Driver register(String fullName, String licenseNumber, LicenseClass licenseClass,
                                  boolean hazmaterialCertified, String carrierId) {
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("fullName must not be blank");
        if (licenseNumber == null || licenseNumber.isBlank()) throw new IllegalArgumentException("licenseNumber must not be blank");
        if (carrierId == null || carrierId.isBlank()) throw new IllegalArgumentException("carrierId must not be blank");

        DriverId id = DriverId.generate();
        Driver driver = new Driver(id, fullName, licenseNumber, licenseClass, hazmaterialCertified, carrierId,
                DriverStatus.AVAILABLE, Map.of());
        driver.registerEvent(DriverRegistered.of(id.toString(), fullName, licenseNumber, licenseClass, hazmaterialCertified, carrierId));
        return driver;
    }

    public static Driver reconstitute(DriverId id, String fullName, String licenseNumber, LicenseClass licenseClass,
                                      boolean hazmaterialCertified, String carrierId, DriverStatus status,
                                      Map<LocalDate, DrivingSession> drivingSessions) {
        return new Driver(id, fullName, licenseNumber, licenseClass, hazmaterialCertified, carrierId, status, drivingSessions);
    }

    public void updateStatus(DriverStatus newStatus, String reason) {
        if (status == newStatus) throw new IllegalStateException("Driver is already in status: " + status);
        DriverStatus previous = this.status;
        this.status = newStatus;
        registerEvent(DriverStatusChanged.of(id.toString(), previous, newStatus, reason));
    }

    // BR-005: validates a prospective driving block does not push over 9h/day
    public void recordDriving(LocalDate date, Duration duration) {
        DrivingSession session = drivingSessions.getOrDefault(date, DrivingSession.startingOn(date));
        if (session.wouldExceedLimit(duration)) {
            throw new IllegalStateException(
                    "Driver would exceed 9h daily limit. Already worked: " + session.hoursWorked().toMinutes() + " min");
        }
        drivingSessions.put(date, session.addHours(duration));
    }

    public boolean canDriveHazmat() { return hazmaterialCertified; }

    public boolean isAvailable() { return status == DriverStatus.AVAILABLE; }

    public DrivingSession getDrivingSessionFor(LocalDate date) {
        return drivingSessions.getOrDefault(date, DrivingSession.startingOn(date));
    }

    public DriverId getId() { return id; }
    public String getFullName() { return fullName; }
    public String getLicenseNumber() { return licenseNumber; }
    public LicenseClass getLicenseClass() { return licenseClass; }
    public boolean isHazmaterialCertified() { return hazmaterialCertified; }
    public String getCarrierId() { return carrierId; }
    public DriverStatus getStatus() { return status; }
    public Map<LocalDate, DrivingSession> getDrivingSessions() { return Map.copyOf(drivingSessions); }
}
