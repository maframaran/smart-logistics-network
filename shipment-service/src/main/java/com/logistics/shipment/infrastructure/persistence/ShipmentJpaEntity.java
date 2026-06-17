package com.logistics.shipment.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "shipments", schema = "shipment")
class ShipmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    UUID id;

    @Column(name = "shipper_id", nullable = false)
    String shipperId;

    // origin
    @Column(name = "origin_street") String originStreet;
    @Column(name = "origin_city") String originCity;
    @Column(name = "origin_state") String originState;
    @Column(name = "origin_postal_code") String originPostalCode;
    @Column(name = "origin_country") String originCountry;
    @Column(name = "origin_latitude") double originLatitude;
    @Column(name = "origin_longitude") double originLongitude;

    // destination
    @Column(name = "destination_street") String destinationStreet;
    @Column(name = "destination_city") String destinationCity;
    @Column(name = "destination_state") String destinationState;
    @Column(name = "destination_postal_code") String destinationPostalCode;
    @Column(name = "destination_country") String destinationCountry;
    @Column(name = "destination_latitude") double destinationLatitude;
    @Column(name = "destination_longitude") double destinationLongitude;

    // cargo
    @Column(name = "weight_kg") double weightKg;
    @Column(name = "volume_m3") double volumeM3;
    @Column(name = "requires_hazmat") boolean requiresHazmat;
    @Column(name = "requires_cold_chain") boolean requiresColdChain;

    @Column(name = "sla_type", nullable = false)
    String slaType;

    @Column(name = "status", nullable = false)
    String status;

    @Column(name = "required_delivery_date")
    LocalDate requiredDeliveryDate;

    @Column(name = "assigned_vehicle_id")
    String assignedVehicleId;

    @Column(name = "assigned_driver_id")
    String assignedDriverId;

    @Column(name = "route_id")
    String routeId;

    @Version
    @Column(name = "version")
    Long version;

    protected ShipmentJpaEntity() {}
}
