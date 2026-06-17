package com.logistics.shipment.infrastructure.rest;

import com.logistics.shipment.domain.model.*;
import com.logistics.shipment.domain.ports.in.CreateShipmentCommand;
import com.logistics.shipment.domain.ports.in.CreateShipmentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

// Inbound Adapter — spec: services/shipment-service/service.md § POST /api/v1/shipments
// Translates HTTP request → Command → calls inbound port → translates result → HTTP response.
// No business logic here. All validation of domain rules happens in the aggregate/use case.
@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final CreateShipmentUseCase createShipmentUseCase;

    public ShipmentController(CreateShipmentUseCase createShipmentUseCase) {
        this.createShipmentUseCase = createShipmentUseCase;
    }

    // POST /api/v1/shipments
    // Request/response schema: services/shipment-service/service.md § POST /api/v1/shipments
    @PostMapping
    public ResponseEntity<CreateShipmentResponse> create(@RequestBody CreateShipmentRequest request) {
        CreateShipmentCommand command = new CreateShipmentCommand(
                toAddress(request.origin()),
                toAddress(request.destination()),
                new CargoSpec(
                        request.cargo().weightKg(),
                        request.cargo().volumeM3(),
                        request.cargo().requiresHazmat(),
                        request.cargo().requiresColdChain()
                ),
                SlaType.valueOf(request.slaType()),
                Instant.parse(request.requiredDeliveryDate())
        );

        ShipmentId shipmentId = createShipmentUseCase.create(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateShipmentResponse(
                        shipmentId.toString(),
                        ShipmentStatus.CREATED.name(),
                        Instant.now().toString()
                ));
    }

    private Address toAddress(CreateShipmentRequest.AddressDto dto) {
        return new Address(dto.street(), dto.city(), dto.country(), new Coordinates(dto.lat(), dto.lon()));
    }
}
