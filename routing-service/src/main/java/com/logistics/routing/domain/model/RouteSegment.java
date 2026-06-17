package com.logistics.routing.domain.model;

public record RouteSegment(
        int order,
        String label,
        Coordinates from,
        Coordinates to,
        double distanceKm,
        long estimatedDurationMinutes
) {
    public RouteSegment {
        if (distanceKm < 0) throw new IllegalArgumentException("distanceKm must not be negative");
        if (estimatedDurationMinutes < 0) throw new IllegalArgumentException("estimatedDurationMinutes must not be negative");
    }
}
