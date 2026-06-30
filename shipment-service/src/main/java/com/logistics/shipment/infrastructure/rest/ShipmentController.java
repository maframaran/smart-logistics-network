package com.logistics.shipment.infrastructure.rest;

import com.logistics.shipment.domain.model.*;
import com.logistics.shipment.domain.ports.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Shipments", description = "Shipment lifecycle: create, assign, cancel, track")
@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final CreateShipmentUseCase createShipment;
    private final AssignShipmentUseCase assignShipment;
    private final CancelShipmentUseCase cancelShipment;
    private final GetShipmentUseCase getShipment;

    public ShipmentController(
            CreateShipmentUseCase createShipment,
            AssignShipmentUseCase assignShipment,
            CancelShipmentUseCase cancelShipment,
            GetShipmentUseCase getShipment
    ) {
        this.createShipment = createShipment;
        this.assignShipment = assignShipment;
        this.cancelShipment = cancelShipment;
        this.getShipment = getShipment;
    }

    @Operation(summary = "Create a shipment", description = "Validates cargo/SLA and creates a shipment in CREATED status; raises ShipmentCreated.")
    @ApiResponse(responseCode = "201", description = "Shipment created")
    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@RequestBody CreateShipmentRequest request) {
        ShipmentId id = createShipment.create(new CreateShipmentUseCase.Command(
                request.shipperId(),
                toAddress(request.origin()),
                toAddress(request.destination()),
                new CargoSpec(request.cargo().weightKg(), request.cargo().volumeM3(), request.cargo().requiresHazmat(), request.cargo().requiresColdChain()),
                SlaType.valueOf(request.slaType()),
                request.requiredDeliveryDate()
        ));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(new ShipmentResponse(id.toString(), "CREATED"));
    }

    @Operation(summary = "Get a shipment by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDetailResponse> get(@PathVariable String id) {
        Shipment shipment = getShipment.findById(ShipmentId.of(id));
        return ResponseEntity.ok(toDetailResponse(shipment));
    }

    @Operation(summary = "List shipments", description = "Optionally filter by status.")
    @GetMapping
    public ResponseEntity<List<ShipmentDetailResponse>> listByStatus(@RequestParam(required = false) String status) {
        List<Shipment> shipments = status != null
                ? getShipment.findByStatus(ShipmentStatus.valueOf(status))
                : getShipment.findByStatus(null);
        return ResponseEntity.ok(shipments.stream().map(this::toDetailResponse).toList());
    }

    @Operation(summary = "Assign a vehicle, driver, and route to a shipment", description = "Only valid in CREATED/SCHEDULED status; raises ShipmentAssigned.")
    @PostMapping("/{id}/assign")
    public ResponseEntity<Void> assign(@PathVariable String id, @RequestBody AssignShipmentRequest request) {
        assignShipment.assign(new AssignShipmentUseCase.Command(
                ShipmentId.of(id),
                request.vehicleId(),
                request.driverId(),
                request.routeId()
        ));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancel a shipment", description = "Not allowed once IN_TRANSIT/DELIVERED/CANCELLED; raises ShipmentCancelled.")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String id, @RequestBody CancelShipmentRequest request) {
        cancelShipment.cancel(new CancelShipmentUseCase.Command(ShipmentId.of(id), request.reason()));
        return ResponseEntity.noContent().build();
    }

    private Address toAddress(AddressRequest r) {
        return new Address(r.street(), r.city(), r.state(), r.postalCode(), r.country(), r.latitude(), r.longitude());
    }

    private ShipmentDetailResponse toDetailResponse(Shipment s) {
        AddressResponse origin = new AddressResponse(
                s.getOrigin().street(), s.getOrigin().city(), s.getOrigin().country(),
                s.getOrigin().latitude(), s.getOrigin().longitude());
        AddressResponse destination = new AddressResponse(
                s.getDestination().street(), s.getDestination().city(), s.getDestination().country(),
                s.getDestination().latitude(), s.getDestination().longitude());
        CargoResponse cargo = new CargoResponse(
                s.getCargoSpec().weightKg(), s.getCargoSpec().volumeM3(),
                s.getCargoSpec().requiresHazmat(), s.getCargoSpec().requiresColdChain());
        return new ShipmentDetailResponse(
                s.getId().toString(),
                s.getShipperId(),
                s.getStatus().name(),
                s.getSlaType().name(),
                s.getRequiredDeliveryDate(),
                s.getAssignedVehicleId(),
                s.getAssignedDriverId(),
                s.getRouteId(),
                origin,
                destination,
                cargo
        );
    }

    // ── Request / Response records ────────────────────────────────────────────

    record CreateShipmentRequest(
            String shipperId,
            AddressRequest origin,
            AddressRequest destination,
            CargoRequest cargo,
            String slaType,
            LocalDate requiredDeliveryDate
    ) {}

    record AddressRequest(
            String street, String city, String state,
            String postalCode, String country,
            double latitude, double longitude
    ) {}

    record CargoRequest(
            double weightKg, double volumeM3,
            boolean requiresHazmat, boolean requiresColdChain
    ) {}

    record AssignShipmentRequest(String vehicleId, String driverId, String routeId) {}

    record CancelShipmentRequest(String reason) {}

    record ShipmentResponse(String shipmentId, String status) {}

    record AddressResponse(String street, String city, String country, double lat, double lon) {}
    record CargoResponse(double weightKg, double volumeM3, boolean requiresHazmat, boolean requiresColdChain) {}

    record ShipmentDetailResponse(
            String shipmentId,
            String shipperId,
            String status,
            String slaType,
            LocalDate requiredDeliveryDate,
            String assignedVehicleId,
            String assignedDriverId,
            String routeId,
            AddressResponse origin,
            AddressResponse destination,
            CargoResponse cargo
    ) {}
}
