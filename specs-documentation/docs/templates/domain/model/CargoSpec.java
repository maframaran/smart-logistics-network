package com.logistics.shipment.domain.model;

// Value Object — spec: architecture/domains.md § Shipment Domain
// Immutable cargo characteristics. Enforces BR-001 and BR-002 at construction time.
public record CargoSpec(
        double weightKg,
        double volumeM3,
        boolean requiresHazmat,
        boolean requiresColdChain
) {
    // BR-001: weight must be positive (spec: specs/features/F-001-create-shipment.md § Edge Cases EC-003)
    // BR-002: volume must be positive
    public CargoSpec {
        if (weightKg <= 0) throw new CargoSpecInvalidException("weightKg must be positive, got: " + weightKg);
        if (volumeM3 <= 0) throw new CargoSpecInvalidException("volumeM3 must be positive, got: " + volumeM3);
    }
}
