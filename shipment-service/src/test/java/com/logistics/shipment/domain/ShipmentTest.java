package com.logistics.shipment.domain;

import com.logistics.shipment.domain.events.ShipmentAssigned;
import com.logistics.shipment.domain.events.ShipmentCancelled;
import com.logistics.shipment.domain.events.ShipmentCreated;
import com.logistics.shipment.domain.model.*;
import com.logistics.common.domain.DomainEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ShipmentTest {

    private static final Address ORIGIN = new Address("123 Main St", "São Paulo", "SP", "01001-000", "BR", -23.5505, -46.6333);
    private static final Address DESTINATION = new Address("456 Oak Ave", "Rio de Janeiro", "RJ", "20040-020", "BR", -22.9068, -43.1729);
    private static final CargoSpec CARGO = new CargoSpec(100.0, 2.5, false, false);
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(10);

    @Test
    void create_raisesShipmentCreatedEvent() {
        Shipment shipment = Shipment.create("shipper-1", ORIGIN, DESTINATION, CARGO, SlaType.STANDARD, FUTURE_DATE);

        List<DomainEvent> events = shipment.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(ShipmentCreated.class);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CREATED);
    }

    @Test
    void create_withPastDeliveryDate_throws() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertThatThrownBy(() -> Shipment.create("s1", ORIGIN, DESTINATION, CARGO, SlaType.STANDARD, yesterday))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requiredDeliveryDate");
    }

    @Test
    void assign_transitionsToAssignedAndRaisesEvent() {
        Shipment shipment = Shipment.create("shipper-1", ORIGIN, DESTINATION, CARGO, SlaType.PRIORITY, FUTURE_DATE);
        shipment.pullDomainEvents(); // clear

        shipment.assign("vehicle-1", "driver-1", "route-1");

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.ASSIGNED);
        assertThat(shipment.getAssignedVehicleId()).isEqualTo("vehicle-1");
        assertThat(shipment.getAssignedDriverId()).isEqualTo("driver-1");

        List<DomainEvent> events = shipment.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(ShipmentAssigned.class);
    }

    @Test
    void cancel_fromCreated_raisesEventWithNoFee() {
        Shipment shipment = Shipment.create("shipper-1", ORIGIN, DESTINATION, CARGO, SlaType.EXPRESS, FUTURE_DATE);
        shipment.pullDomainEvents();

        shipment.cancel("Customer request");

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        ShipmentCancelled event = (ShipmentCancelled) shipment.pullDomainEvents().getFirst();
        assertThat(event.cancellationFeeApplied()).isFalse();
    }

    @Test
    void cancel_fromAssigned_appliesFee() {
        Shipment shipment = Shipment.create("shipper-1", ORIGIN, DESTINATION, CARGO, SlaType.STANDARD, FUTURE_DATE);
        shipment.pullDomainEvents();
        shipment.assign("v1", "d1", "r1");
        shipment.pullDomainEvents();

        shipment.cancel("Changed plans");

        ShipmentCancelled event = (ShipmentCancelled) shipment.pullDomainEvents().getFirst();
        assertThat(event.cancellationFeeApplied()).isTrue();
    }

    @Test
    void pullDomainEvents_clearsEvents() {
        Shipment shipment = Shipment.create("s1", ORIGIN, DESTINATION, CARGO, SlaType.STANDARD, FUTURE_DATE);

        shipment.pullDomainEvents();

        assertThat(shipment.pullDomainEvents()).isEmpty();
    }

    @Test
    void cargoSpec_withNegativeWeight_throws() {
        assertThatThrownBy(() -> new CargoSpec(-1.0, 2.0, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("weightKg");
    }

    @Test
    void cargoSpec_withZeroVolume_throws() {
        assertThatThrownBy(() -> new CargoSpec(10.0, 0.0, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("volumeM3");
    }
}
