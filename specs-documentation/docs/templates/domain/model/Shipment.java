package com.logistics.shipment.domain.model;

import com.logistics.shipment.domain.events.ShipmentCreated;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Aggregate Root — spec: architecture/domains.md § Shipment Domain
// Owns its own invariants. All state changes go through public methods.
// Domain events are collected here and published by the use case after save.
public class Shipment {

    private final ShipmentId id;
    private final Address origin;
    private final Address destination;
    private final CargoSpec cargoSpec;
    private final SlaType slaType;
    private final Instant requiredDeliveryDate;
    private ShipmentStatus status;

    // Collected domain events — cleared after publishing (see CreateShipmentService)
    private final List<Object> domainEvents = new ArrayList<>();

    private Shipment(ShipmentId id, Address origin, Address destination,
                     CargoSpec cargoSpec, SlaType slaType, Instant requiredDeliveryDate) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.cargoSpec = cargoSpec;
        this.slaType = slaType;
        this.requiredDeliveryDate = requiredDeliveryDate;
        this.status = ShipmentStatus.CREATED;
    }

    // Factory method — spec: specs/features/F-001-create-shipment.md § Workflow step 4
    // CargoSpec constructor already enforced BR-001/BR-002.
    // requiredDeliveryDate validation is in the use case (needs clock reference).
    public static Shipment create(Address origin, Address destination,
                                  CargoSpec cargoSpec, SlaType slaType,
                                  Instant requiredDeliveryDate) {
        ShipmentId id = ShipmentId.generate();
        Shipment shipment = new Shipment(id, origin, destination, cargoSpec, slaType, requiredDeliveryDate);

        // Raise domain event — spec: specs/features/F-001-create-shipment.md § AC-002
        shipment.domainEvents.add(new ShipmentCreated(
                id,
                origin,
                destination,
                cargoSpec,
                slaType,
                requiredDeliveryDate,
                Instant.now()
        ));

        return shipment;
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = Collections.unmodifiableList(new ArrayList<>(domainEvents));
        domainEvents.clear();
        return events;
    }

    // Getters (no setters — state changes only through domain methods)
    public ShipmentId getId() { return id; }
    public ShipmentStatus getStatus() { return status; }
    public CargoSpec getCargoSpec() { return cargoSpec; }
    public SlaType getSlaType() { return slaType; }
    public Instant getRequiredDeliveryDate() { return requiredDeliveryDate; }
    public Address getOrigin() { return origin; }
    public Address getDestination() { return destination; }
}
