package com.logistics.fleet.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "vehicles", schema = "fleet")
class VehicleJpaEntity {

    @Id UUID id;
    @Column(nullable = false, unique = true) String licensePlate;
    @Column(nullable = false) String type;
    @Column(nullable = false) double maxWeightKg;
    @Column(name = "max_volume_m3", nullable = false) double maxVolumeM3;
    @Column(nullable = false) String carrierId;
    @Column(nullable = false) String status;
    @Version Long version;

    protected VehicleJpaEntity() {}
}
