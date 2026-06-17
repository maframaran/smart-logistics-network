package com.logistics.billing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface InvoiceJpaRepositoryPort extends JpaRepository<InvoiceJpaEntity, UUID> {
    Optional<InvoiceJpaEntity> findByShipmentId(String shipmentId);
    List<InvoiceJpaEntity> findByStatus(String status);
}
