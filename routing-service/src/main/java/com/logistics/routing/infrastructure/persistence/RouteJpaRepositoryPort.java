package com.logistics.routing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface RouteJpaRepositoryPort extends JpaRepository<RouteJpaEntity, UUID> {
    Optional<RouteJpaEntity> findByShipmentId(String shipmentId);
}
