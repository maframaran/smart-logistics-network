package com.logistics.shipment.domain.model;

import com.logistics.common.domain.AggregateRoot;
import com.logistics.shipment.domain.events.ShipmentAssigned;
import com.logistics.shipment.domain.events.ShipmentCancelled;
import com.logistics.shipment.domain.events.ShipmentCreated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Shipment extends AggregateRoot {

    private final ShipmentId id;
    private final String shipperId;
    private final Address origin;
    private final Address destination;
    private final CargoSpec cargoSpec;
    private final SlaType slaType;
    private final LocalDate requiredDeliveryDate;
    private ShipmentStatus status;

    // Assignment state (null until assigned)
    private String assignedVehicleId;
    private String assignedDriverId;
    private String routeId;

    @Builder(access = AccessLevel.PRIVATE)
    private Shipment(
            ShipmentId id,
            String shipperId,
            Address origin,
            Address destination,
            CargoSpec cargoSpec,
            SlaType slaType,
            LocalDate requiredDeliveryDate,
            ShipmentStatus status
    ) {
        this.id = id;
        this.shipperId = shipperId;
        this.origin = origin;
        this.destination = destination;
        this.cargoSpec = cargoSpec;
        this.slaType = slaType;
        this.requiredDeliveryDate = requiredDeliveryDate;
        this.status = status;
    }

    public static Shipment create(
            String shipperId,
            Address origin,
            Address destination,
            CargoSpec cargoSpec,
            SlaType slaType,
            LocalDate requiredDeliveryDate
    ) {
        if (requiredDeliveryDate == null || !requiredDeliveryDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("requiredDeliveryDate must be in the future");
        }

        ShipmentId id = ShipmentId.generate();
        Shipment shipment = Shipment.builder()
                .id(id).shipperId(shipperId).origin(origin).destination(destination)
                .cargoSpec(cargoSpec).slaType(slaType).requiredDeliveryDate(requiredDeliveryDate)
                .status(ShipmentStatus.CREATED)
                .build();
        shipment.registerEvent(ShipmentCreated.of(
                id.toString(), shipperId, origin, destination, cargoSpec, slaType, requiredDeliveryDate
        ));
        return shipment;
    }

    // Reconstitute from persistence (no events raised)
    public static Shipment reconstitute(
            ShipmentId id,
            String shipperId,
            Address origin,
            Address destination,
            CargoSpec cargoSpec,
            SlaType slaType,
            LocalDate requiredDeliveryDate,
            ShipmentStatus status,
            String assignedVehicleId,
            String assignedDriverId,
            String routeId
    ) {
        Shipment shipment = Shipment.builder()
                .id(id).shipperId(shipperId).origin(origin).destination(destination)
                .cargoSpec(cargoSpec).slaType(slaType).requiredDeliveryDate(requiredDeliveryDate)
                .status(status)
                .build();
        shipment.assignedVehicleId = assignedVehicleId;
        shipment.assignedDriverId = assignedDriverId;
        shipment.routeId = routeId;
        return shipment;
    }

    public void assign(String vehicleId, String driverId, String routeId) {
        if (status != ShipmentStatus.CREATED && status != ShipmentStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot assign shipment in status: " + status);
        }
        this.assignedVehicleId = vehicleId;
        this.assignedDriverId = driverId;
        this.routeId = routeId;
        this.status = ShipmentStatus.ASSIGNED;
        registerEvent(ShipmentAssigned.of(id.toString(), vehicleId, driverId, routeId));
    }

    public void cancel(String reason) {
        if (status == ShipmentStatus.IN_TRANSIT || status == ShipmentStatus.DELIVERED || status == ShipmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel shipment in status: " + status);
        }
        boolean feeApplied = status == ShipmentStatus.SCHEDULED || status == ShipmentStatus.ASSIGNED;
        this.status = ShipmentStatus.CANCELLED;
        registerEvent(ShipmentCancelled.of(id.toString(), reason, feeApplied));
    }
}
