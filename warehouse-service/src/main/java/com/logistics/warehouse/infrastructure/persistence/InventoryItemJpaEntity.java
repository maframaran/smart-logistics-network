package com.logistics.warehouse.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "inventory_items", schema = "warehouse")
class InventoryItemJpaEntity {

    @Id UUID id;
    @Column(nullable = false) UUID warehouseId;
    @Column(nullable = false) String sku;
    @Column(nullable = false) String description;
    @Column(nullable = false) double weightKg;
    @Column(name = "volume_m3", nullable = false) double volumeM3;
    @Column(nullable = false) int quantity;

    protected InventoryItemJpaEntity() {}
}
