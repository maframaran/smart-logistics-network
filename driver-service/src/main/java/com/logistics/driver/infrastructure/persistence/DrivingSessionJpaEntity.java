package com.logistics.driver.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "driving_sessions", schema = "driver")
class DrivingSessionJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false) UUID driverId;
    @Column(nullable = false) LocalDate date;
    @Column(nullable = false) long hoursWorkedMinutes;

    protected DrivingSessionJpaEntity() {}

    DrivingSessionJpaEntity(UUID driverId, LocalDate date, long hoursWorkedMinutes) {
        this.driverId = driverId;
        this.date = date;
        this.hoursWorkedMinutes = hoursWorkedMinutes;
    }
}
