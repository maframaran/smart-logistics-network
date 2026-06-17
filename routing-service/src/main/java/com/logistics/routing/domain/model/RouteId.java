package com.logistics.routing.domain.model;

import java.util.UUID;

public record RouteId(UUID value) {
    public RouteId { if (value == null) throw new IllegalArgumentException("RouteId must not be null"); }
    public static RouteId generate() { return new RouteId(UUID.randomUUID()); }
    public static RouteId of(String uuid) { return new RouteId(UUID.fromString(uuid)); }
    @Override public String toString() { return value.toString(); }
}
