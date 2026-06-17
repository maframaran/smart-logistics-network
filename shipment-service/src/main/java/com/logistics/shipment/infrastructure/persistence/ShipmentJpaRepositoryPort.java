package com.logistics.shipment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ShipmentJpaRepositoryPort extends JpaRepository<ShipmentJpaEntity, UUID> {

    List<ShipmentJpaEntity> findByStatus(String status);
}
