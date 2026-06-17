package com.logistics.fleet.domain.model;

public record Capacity(double maxWeightKg, double maxVolumeM3) {

    public Capacity {
        if (maxWeightKg <= 0) throw new IllegalArgumentException("maxWeightKg must be positive, got: " + maxWeightKg);
        if (maxVolumeM3 <= 0) throw new IllegalArgumentException("maxVolumeM3 must be positive, got: " + maxVolumeM3);
    }

    public boolean canAccommodate(double weightKg, double volumeM3) {
        return weightKg <= maxWeightKg && volumeM3 <= maxVolumeM3;
    }
}
