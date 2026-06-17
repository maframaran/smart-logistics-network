package com.logistics.fleet.infrastructure.rest;

import com.logistics.fleet.domain.model.*;
import com.logistics.fleet.domain.ports.in.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final RegisterVehicleUseCase registerVehicle;
    private final UpdateVehicleStatusUseCase updateStatus;
    private final GetVehicleUseCase getVehicle;

    public VehicleController(RegisterVehicleUseCase registerVehicle, UpdateVehicleStatusUseCase updateStatus, GetVehicleUseCase getVehicle) {
        this.registerVehicle = registerVehicle;
        this.updateStatus = updateStatus;
        this.getVehicle = getVehicle;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> register(@RequestBody RegisterVehicleRequest request) {
        VehicleId id = registerVehicle.register(new RegisterVehicleUseCase.Command(
                request.licensePlate(),
                VehicleType.valueOf(request.type()),
                new Capacity(request.maxWeightKg(), request.maxVolumeM3()),
                request.carrierId()
        ));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(new VehicleResponse(id.toString(), "AVAILABLE"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDetailResponse> get(@PathVariable String id) {
        Vehicle v = getVehicle.findById(VehicleId.of(id));
        return ResponseEntity.ok(toDetail(v));
    }

    @GetMapping
    public ResponseEntity<List<VehicleDetailResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") double weightKg,
            @RequestParam(required = false, defaultValue = "0") double volumeM3,
            @RequestParam(required = false, defaultValue = "false") boolean coldChain,
            @RequestParam(required = false, defaultValue = "false") boolean hazmat
    ) {
        List<Vehicle> vehicles = (status != null)
                ? getVehicle.findByStatus(VehicleStatus.valueOf(status))
                : getVehicle.findAvailable(weightKg, volumeM3, coldChain, hazmat);
        return ResponseEntity.ok(vehicles.stream().map(this::toDetail).toList());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest request) {
        updateStatus.update(new UpdateVehicleStatusUseCase.Command(
                VehicleId.of(id),
                VehicleStatus.valueOf(request.status()),
                request.reason()
        ));
        return ResponseEntity.noContent().build();
    }

    private VehicleDetailResponse toDetail(Vehicle v) {
        return new VehicleDetailResponse(v.getId().toString(), v.getLicensePlate(), v.getType().name(),
                v.getCapacity().maxWeightKg(), v.getCapacity().maxVolumeM3(), v.getCarrierId(), v.getStatus().name());
    }

    record RegisterVehicleRequest(String licensePlate, String type, double maxWeightKg, double maxVolumeM3, String carrierId) {}
    record UpdateStatusRequest(String status, String reason) {}
    record VehicleResponse(String vehicleId, String status) {}
    record VehicleDetailResponse(String vehicleId, String licensePlate, String type, double maxWeightKg, double maxVolumeM3, String carrierId, String status) {}
}
