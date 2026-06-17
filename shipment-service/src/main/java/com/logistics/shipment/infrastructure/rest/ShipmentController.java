package com.logistics.shipment.infrastructure.rest;

import com.logistics.shipment.domain.model.*;
import com.logistics.shipment.domain.ports.in.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDetailResponse> get(@PathVariable String id) {
        Shipment shipment = getShipment.findById(ShipmentId.of(id));
        return ResponseEntity.ok(toDetailResponse(shipment));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDetailResponse>> listByStatus(@RequestParam(required = false) String status) {
        List<Shipment> shipments = status != null
                ? getShipment.findByStatus(ShipmentStatus.valueOf(status))
                : getShipment.findByStatus(null);
        return ResponseEntity.ok(shipments.stream().map(this::toDetailResponse).toList());
    }

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

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String id, @RequestBody CancelShipmentRequest request) {
        cancelShipment.cancel(new CancelShipmentUseCase.Command(ShipmentId.of(id), request.reason()));
        return ResponseEntity.noContent().build();
    }

    private Address toAddress(AddressRequest r) {
        return new Address(r.street(), r.city(), r.state(), r.postalCode(), r.country(), r.latitude(), r.longitude());
    }

    private ShipmentDetailResponse toDetailResponse(Shipment s) {
        return new ShipmentDetailResponse(
                s.getId().toString(),
                s.getShipperId(),
                s.getStatus().name(),
                s.getSlaType().name(),
                s.getRequiredDeliveryDate(),
                s.getAssignedVehicleId(),
                s.getAssignedDriverId()
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

    record ShipmentDetailResponse(
            String shipmentId,
            String shipperId,
            String status,
            String slaType,
            LocalDate requiredDeliveryDate,
            String assignedVehicleId,
            String assignedDriverId
    ) {}
}
