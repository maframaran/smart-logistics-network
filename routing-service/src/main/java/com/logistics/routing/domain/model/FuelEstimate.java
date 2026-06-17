package com.logistics.routing.domain.model;

public record FuelEstimate(double litres, double costBrl) {
    public FuelEstimate {
        if (litres < 0) throw new IllegalArgumentException("litres must not be negative");
        if (costBrl < 0) throw new IllegalArgumentException("costBrl must not be negative");
    }
}
