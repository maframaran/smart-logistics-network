package com.logistics.shipment.infrastructure.persistence;

import com.logistics.shipment.domain.model.Shipment;
import com.logistics.shipment.domain.model.ShipmentId;
import com.logistics.shipment.domain.model.ShipmentStatus;
import com.logistics.shipment.domain.ports.out.ShipmentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Outbound Adapter — implements domain port ShipmentRepository.
// Translates between the domain aggregate (Shipment) and the JPA entity (ShipmentJpaEntity).
// The domain has zero knowledge of this class — dependency direction is inward only.
@Repository
public class ShipmentJpaRepository implements ShipmentRepository {

    private final ShipmentJpaEntityRepository jpa; // Spring Data JPA interface

    public ShipmentJpaRepository(ShipmentJpaEntityRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Shipment shipment) {
        jpa.save(ShipmentJpaEntity.fromDomain(shipment));
    }

    @Override
    public Optional<Shipment> findById(ShipmentId id) {
        return jpa.findById(id.value()).map(ShipmentJpaEntity::toDomain);
    }

    @Override
    public List<Shipment> findByStatus(ShipmentStatus status) {
        return jpa.findByStatus(status.name()).stream()
                .map(ShipmentJpaEntity::toDomain)
                .toList();
    }
}

// Spring Data JPA interface — kept in same file for template brevity; split in real code
interface ShipmentJpaEntityRepository extends org.springframework.data.jpa.repository.JpaRepository<ShipmentJpaEntity, UUID> {
    List<ShipmentJpaEntity> findByStatus(String status);
}
