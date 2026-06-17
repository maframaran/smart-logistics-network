package com.logistics.warehouse.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

interface WarehouseJpaRepositoryPort extends JpaRepository<WarehouseJpaEntity, UUID> {}
