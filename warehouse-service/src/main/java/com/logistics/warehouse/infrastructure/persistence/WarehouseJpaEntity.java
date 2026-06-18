package com.logistics.warehouse.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "warehouses", schema = "warehouse")
class WarehouseJpaEntity {

    @Id UUID id;
    @Column(nullable = false) String name;
    @Column(nullable = false) String location;
    @Column(nullable = false) double maxWeightKg;
    @Column(name = "max_volume_m3", nullable = false) double maxVolumeM3;
    @Version Long version;

    @OneToMany(mappedBy = "warehouseId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    List<InventoryItemJpaEntity> items = new ArrayList<>();

    protected WarehouseJpaEntity() {}
}
