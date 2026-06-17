package com.logistics.fleet.domain.model;

import com.logistics.common.domain.AggregateRoot;
import com.logistics.fleet.domain.events.VehicleRegistered;
import com.logistics.fleet.domain.events.VehicleStatusChanged;

public class Vehicle extends AggregateRoot {

    private final VehicleId id;
    private final String licensePlate;
    private final VehicleType type;
    private final Capacity capacity;
    private final String carrierId;
    private VehicleStatus status;

    private Vehicle(VehicleId id, String licensePlate, VehicleType type, Capacity capacity, String carrierId, VehicleStatus status) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.type = type;
        this.capacity = capacity;
        this.carrierId = carrierId;
        this.status = status;
    }

    public static Vehicle register(String licensePlate, VehicleType type, Capacity capacity, String carrierId) {
        if (licensePlate == null || licensePlate.isBlank()) throw new IllegalArgumentException("licensePlate must not be blank");
        if (carrierId == null || carrierId.isBlank()) throw new IllegalArgumentException("carrierId must not be blank");

        VehicleId id = VehicleId.generate();
        Vehicle vehicle = new Vehicle(id, licensePlate, type, capacity, carrierId, VehicleStatus.AVAILABLE);
        vehicle.registerEvent(VehicleRegistered.of(id.toString(), licensePlate, type, capacity, carrierId));
        return vehicle;
    }

    public static Vehicle reconstitute(VehicleId id, String licensePlate, VehicleType type, Capacity capacity, String carrierId, VehicleStatus status) {
        return new Vehicle(id, licensePlate, type, capacity, carrierId, status);
    }

    public void updateStatus(VehicleStatus newStatus, String reason) {
        if (status == newStatus) throw new IllegalStateException("Vehicle is already in status: " + status);
        VehicleStatus previous = this.status;
        this.status = newStatus;
        registerEvent(VehicleStatusChanged.of(id.toString(), previous, newStatus, reason));
    }

    // BR-001/002: check cargo fits this vehicle
    public boolean canCarry(double weightKg, double volumeM3) {
        return capacity.canAccommodate(weightKg, volumeM3);
    }

    // BR-008: cold-chain requires refrigerated truck
    public boolean supportsColdChain() {
        return type == VehicleType.REFRIGERATED_TRUCK;
    }

    // BR-003 (vehicle side): hazmat requires hazmat-certified truck
    public boolean supportsHazmat() {
        return type == VehicleType.HAZMAT_TRUCK;
    }

    public VehicleId getId() { return id; }
    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }
    public Capacity getCapacity() { return capacity; }
    public String getCarrierId() { return carrierId; }
    public VehicleStatus getStatus() { return status; }
}
