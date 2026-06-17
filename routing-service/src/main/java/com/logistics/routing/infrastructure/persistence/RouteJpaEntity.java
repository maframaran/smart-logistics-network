package com.logistics.routing.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routes", schema = "routing")
class RouteJpaEntity {

    @Id UUID id;
    @Column(nullable = false) String shipmentId;
    @Column(nullable = false) String vehicleType;
    double originLatitude;
    double originLongitude;
    double destinationLatitude;
    double destinationLongitude;
    @Column(nullable = false) double totalDistanceKm;
    @Column(nullable = false) long totalDurationMinutes;
    @Column(nullable = false) Instant estimatedArrival;
    double fuelLitres;
    double fuelCostBrl;
    double tollsCostBrl;
    @Column(nullable = false) String status;
    @Version Long version;

    @OneToMany(mappedBy = "routeId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("segmentOrder ASC")
    List<RouteSegmentJpaEntity> segments = new ArrayList<>();

    protected RouteJpaEntity() {}
}
