package com.logistics.driver.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.domain.DomainEvent;
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
    private final OutboxJpaRepositoryPort outboxJpa;
    private final ObjectMapper objectMapper;

    public DriverJpaRepository(DriverJpaRepositoryPort driverJpa, DrivingSessionJpaRepositoryPort sessionJpa,
                                OutboxJpaRepositoryPort outboxJpa, ObjectMapper objectMapper) {
        this.driverJpa = driverJpa;
        this.sessionJpa = sessionJpa;
        this.outboxJpa = outboxJpa;
        this.objectMapper = objectMapper;
    }

    // Writes the aggregate and its pulled domain events as outbox rows in the same
    // transaction (ADR-030) — atomic with the aggregate write since @Transactional here
    // nests into the calling use case's transaction by default propagation.
    @Override
    @Transactional
    public void save(Driver driver) {
        driverJpa.save(toEntity(driver));
        sessionJpa.deleteByDriverId(driver.getId().value());
        driver.getDrivingSessions().forEach((date, session) ->
                sessionJpa.save(new DrivingSessionJpaEntity(driver.getId().value(), date, session.hoursWorked().toMinutes()))
        );
        for (DomainEvent event : driver.pullDomainEvents()) {
            outboxJpa.save(toOutboxEntity(event));
        }
    }

    private OutboxEventEntity toOutboxEntity(DomainEvent event) {
        OutboxEventEntity e = new OutboxEventEntity();
        e.aggregateId = event.aggregateId();
        e.eventType = event.getClass().getSimpleName();
        e.occurredAt = event.occurredAt();
        try {
            e.payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize domain event for outbox: " + event.getClass().getSimpleName(), ex);
        }
        return e;
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
