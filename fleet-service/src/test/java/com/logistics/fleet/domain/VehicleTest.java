package com.logistics.fleet.domain;

import com.logistics.fleet.domain.events.VehicleRegistered;
import com.logistics.fleet.domain.events.VehicleStatusChanged;
import com.logistics.fleet.domain.model.*;
import com.logistics.common.domain.DomainEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class VehicleTest {

    private static final Capacity CAPACITY = new Capacity(5000.0, 20.0);

    @Test
    void register_raisesVehicleRegisteredEvent() {
        Vehicle vehicle = Vehicle.register("ABC-1234", VehicleType.TRUCK, CAPACITY, "carrier-1");

        List<DomainEvent> events = vehicle.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(VehicleRegistered.class);
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    void register_withBlankLicensePlate_throws() {
        assertThatThrownBy(() -> Vehicle.register("", VehicleType.VAN, CAPACITY, "carrier-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("licensePlate");
    }

    @Test
    void updateStatus_raisesStatusChangedEvent() {
        Vehicle vehicle = Vehicle.register("XYZ-9999", VehicleType.TRUCK, CAPACITY, "carrier-2");
        vehicle.pullDomainEvents();

        vehicle.updateStatus(VehicleStatus.MAINTENANCE, "Scheduled service");

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
        VehicleStatusChanged event = (VehicleStatusChanged) vehicle.pullDomainEvents().getFirst();
        assertThat(event.previousStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(event.newStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
    }

    @Test
    void updateStatus_toSameStatus_throws() {
        Vehicle vehicle = Vehicle.register("DUP-1111", VehicleType.VAN, CAPACITY, "carrier-1");
        assertThatThrownBy(() -> vehicle.updateStatus(VehicleStatus.AVAILABLE, "same"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCarry_withinCapacity_returnsTrue() {
        Vehicle vehicle = Vehicle.register("CAP-0001", VehicleType.TRUCK, CAPACITY, "c1");
        assertThat(vehicle.canCarry(4999.0, 19.9)).isTrue();
    }

    @Test
    void canCarry_exceedsWeight_returnsFalse() {
        Vehicle vehicle = Vehicle.register("CAP-0002", VehicleType.TRUCK, CAPACITY, "c1");
        assertThat(vehicle.canCarry(5001.0, 1.0)).isFalse();
    }

    @Test
    void coldChain_onlyRefrigeratedTruck_returnsTrue() {
        Vehicle refrigerated = Vehicle.register("R-001", VehicleType.REFRIGERATED_TRUCK, CAPACITY, "c1");
        Vehicle regular = Vehicle.register("T-001", VehicleType.TRUCK, CAPACITY, "c1");
        assertThat(refrigerated.supportsColdChain()).isTrue();
        assertThat(regular.supportsColdChain()).isFalse();
    }

    @Test
    void hazmat_onlyHazmatTruck_returnsTrue() {
        Vehicle hazmat = Vehicle.register("H-001", VehicleType.HAZMAT_TRUCK, CAPACITY, "c1");
        Vehicle regular = Vehicle.register("T-002", VehicleType.TRUCK, CAPACITY, "c1");
        assertThat(hazmat.supportsHazmat()).isTrue();
        assertThat(regular.supportsHazmat()).isFalse();
    }

    @Test
    void capacity_withNegativeWeight_throws() {
        assertThatThrownBy(() -> new Capacity(-1.0, 10.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxWeightKg");
    }
}
