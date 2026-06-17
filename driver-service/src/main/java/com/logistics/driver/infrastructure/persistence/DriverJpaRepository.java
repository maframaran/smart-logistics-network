package com.logistics.driver.infrastructure.persistence;

import com.logistics.driver.domain.model.*;
import com.logistics.driver.domain.ports.out.DriverRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Repository
public class DriverJpaRepository implements DriverRepository {

    private final DriverJpaRepositoryPort driverJpa;
    private final DrivingSessionJpaRepositoryPort sessionJpa;

    public DriverJpaRepository(DriverJpaRepositoryPort driverJpa, DrivingSessionJpaRepositoryPort sessionJpa) {
        this.driverJpa = driverJpa;
        this.sessionJpa = sessionJpa;
    }

    @Override
    @Transactional
    public void save(Driver driver) {
        driverJpa.save(toEntity(driver));
        sessionJpa.deleteByDriverId(driver.getId().value());
        driver.getDrivingSessions().forEach((date, session) ->
                sessionJpa.save(new DrivingSessionJpaEntity(driver.getId().value(), date, session.hoursWorked().toMinutes()))
        );
    }

    @Override
    public Optional<Driver> findById(DriverId id) {
        return driverJpa.findById(id.value()).map(e -> {
            List<DrivingSessionJpaEntity> sessions = sessionJpa.findByDriverId(e.id);
            return toDomain(e, sessions);
        });
    }

    @Override
    public List<Driver> findByStatus(DriverStatus status) {
        return driverJpa.findByStatus(status.name()).stream().map(e -> {
            List<DrivingSessionJpaEntity> sessions = sessionJpa.findByDriverId(e.id);
            return toDomain(e, sessions);
        }).toList();
    }

    private DriverJpaEntity toEntity(Driver d) {
        DriverJpaEntity e = new DriverJpaEntity();
        e.id = d.getId().value();
        e.fullName = d.getFullName();
        e.licenseNumber = d.getLicenseNumber();
        e.licenseClass = d.getLicenseClass().name();
        e.hazmaterialCertified = d.isHazmaterialCertified();
        e.carrierId = d.getCarrierId();
        e.status = d.getStatus().name();
        return e;
    }

    private Driver toDomain(DriverJpaEntity e, List<DrivingSessionJpaEntity> sessions) {
        Map<LocalDate, DrivingSession> drivingSessions = new HashMap<>();
        for (DrivingSessionJpaEntity s : sessions) {
            drivingSessions.put(s.date, new DrivingSession(s.date, Duration.ofMinutes(s.hoursWorkedMinutes)));
        }
        return Driver.reconstitute(
                new DriverId(e.id),
                e.fullName,
                e.licenseNumber,
                LicenseClass.valueOf(e.licenseClass),
                e.hazmaterialCertified,
                e.carrierId,
                DriverStatus.valueOf(e.status),
                drivingSessions
        );
    }
}
