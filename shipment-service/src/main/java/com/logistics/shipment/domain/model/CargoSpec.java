package com.logistics.shipment.domain.model;

public record CargoSpec(
        double weightKg,
        double volumeM3,
        boolean requiresHazmat,
        boolean requiresColdChain
) {
    public CargoSpec {
        if (weightKg <= 0) throw new IllegalArgumentException("weightKg must be positive, got: " + weightKg);
        if (volumeM3 <= 0) throw new IllegalArgumentException("volumeM3 must be positive, got: " + volumeM3);
    }
}
