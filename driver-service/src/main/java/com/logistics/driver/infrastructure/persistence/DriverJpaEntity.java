package com.logistics.driver.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "drivers", schema = "driver")
class DriverJpaEntity {

    @Id UUID id;
    @Column(nullable = false) String fullName;
    @Column(nullable = false, unique = true) String licenseNumber;
    @Column(nullable = false) String licenseClass;
    @Column(nullable = false) boolean hazmaterialCertified;
    @Column(nullable = false) String carrierId;
    @Column(nullable = false) String status;
    @Version Long version;

    protected DriverJpaEntity() {}
}
