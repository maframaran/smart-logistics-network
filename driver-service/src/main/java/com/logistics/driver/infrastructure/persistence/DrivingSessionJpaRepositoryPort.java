package com.logistics.driver.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface DrivingSessionJpaRepositoryPort extends JpaRepository<DrivingSessionJpaEntity, Long> {
    List<DrivingSessionJpaEntity> findByDriverId(UUID driverId);
    void deleteByDriverId(UUID driverId);
}
