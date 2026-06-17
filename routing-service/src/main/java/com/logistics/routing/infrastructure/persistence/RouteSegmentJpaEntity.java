package com.logistics.routing.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "route_segments", schema = "routing")
class RouteSegmentJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false) UUID routeId;
    @Column(nullable = false) int segmentOrder;
    String label;
    double fromLatitude;
    double fromLongitude;
    double toLatitude;
    double toLongitude;
    double distanceKm;
    long estimatedDurationMinutes;

    protected RouteSegmentJpaEntity() {}
}
