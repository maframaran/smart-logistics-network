package com.logistics.fleet.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface VehicleJpaRepositoryPort extends JpaRepository<VehicleJpaEntity, UUID> {
    List<VehicleJpaEntity> findByStatus(String status);
}
