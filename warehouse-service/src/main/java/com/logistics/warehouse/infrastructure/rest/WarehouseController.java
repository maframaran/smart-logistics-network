package com.logistics.warehouse.infrastructure.rest;

import com.logistics.warehouse.domain.model.*;
import com.logistics.warehouse.domain.ports.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Warehouses", description = "Inventory management and BR-006 capacity")
@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

    private final CreateWarehouseUseCase createWarehouse;
    private final ReceiveInventoryUseCase receiveInventory;
    private final DispatchInventoryUseCase dispatchInventory;
    private final GetWarehouseUseCase getWarehouse;

    public WarehouseController(CreateWarehouseUseCase createWarehouse, ReceiveInventoryUseCase receiveInventory,
                               DispatchInventoryUseCase dispatchInventory, GetWarehouseUseCase getWarehouse) {
        this.createWarehouse = createWarehouse;
        this.receiveInventory = receiveInventory;
        this.dispatchInventory = dispatchInventory;
        this.getWarehouse = getWarehouse;
    }

    @Operation(summary = "Create a warehouse")
    @ApiResponse(responseCode = "201", description = "Warehouse created")
    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@RequestBody CreateWarehouseRequest request) {
        WarehouseId id = createWarehouse.create(new CreateWarehouseUseCase.Command(
                request.name(), request.location(), request.maxWeightKg(), request.maxVolumeM3()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(new WarehouseResponse(id.toString()));
    }

    @Operation(summary = "Get a warehouse by ID")
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDetailResponse> get(@PathVariable String id) {
        Warehouse w = getWarehouse.findById(WarehouseId.of(id));
        return ResponseEntity.ok(toDetail(w));
    }

    @Operation(summary = "List all warehouses")
    @GetMapping
    public ResponseEntity<List<WarehouseDetailResponse>> list() {
        return ResponseEntity.ok(getWarehouse.findAll().stream().map(this::toDetail).toList());
    }

    @Operation(summary = "Receive inventory into a warehouse", description = "Validates against BR-006 capacity limits.")
    @PostMapping("/{id}/inventory/receive")
    public ResponseEntity<Void> receive(@PathVariable String id, @RequestBody ReceiveInventoryRequest request) {
        receiveInventory.receive(new ReceiveInventoryUseCase.Command(
                WarehouseId.of(id), request.sku(), request.description(),
                request.weightKg(), request.volumeM3(), request.quantity()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Dispatch inventory from a warehouse")
    @PostMapping("/{id}/inventory/dispatch")
    public ResponseEntity<Void> dispatch(@PathVariable String id, @RequestBody DispatchInventoryRequest request) {
        dispatchInventory.dispatch(new DispatchInventoryUseCase.Command(WarehouseId.of(id), request.sku(), request.quantity()));
        return ResponseEntity.noContent().build();
    }

    private WarehouseDetailResponse toDetail(Warehouse w) {
        List<InventoryItemResponse> items = w.getInventory().values().stream()
                .map(i -> new InventoryItemResponse(i.sku().value(), i.description(), i.quantity(), i.totalWeightKg(), i.totalVolumeM3()))
                .toList();
        double weightFill = w.getMaxWeightKg() > 0 ? Math.round(w.currentWeightKg() / w.getMaxWeightKg() * 100.0) : 0;
        double volumeFill = w.getMaxVolumeM3() > 0 ? Math.round(w.currentVolumeM3() / w.getMaxVolumeM3() * 100.0) : 0;
        String locationCity = w.getLocation().contains(",") ? w.getLocation().split(",")[0].trim() : w.getLocation();
        return new WarehouseDetailResponse(w.getId().toString(), w.getName(), w.getLocation(),
                locationCity, w.getMaxWeightKg(), w.getMaxVolumeM3(),
                w.currentWeightKg(), w.currentVolumeM3(),
                w.availableWeightKg(), w.availableVolumeM3(),
                weightFill, volumeFill, items);
    }

    record CreateWarehouseRequest(String name, String location, double maxWeightKg, double maxVolumeM3) {}
    record ReceiveInventoryRequest(String sku, String description, double weightKg, double volumeM3, int quantity) {}
    record DispatchInventoryRequest(String sku, int quantity) {}
    record WarehouseResponse(String warehouseId) {}
    record InventoryItemResponse(String sku, String description, int quantity, double totalWeightKg, double totalVolumeM3) {}
    record WarehouseDetailResponse(String warehouseId, String name, String location,
                                   String locationCity,
                                   double maxWeightKg, double maxVolumeM3,
                                   double currentWeightKg, double currentVolumeM3,
                                   double availableWeightKg, double availableVolumeM3,
                                   double weightFillPercent, double volumeFillPercent,
                                   List<InventoryItemResponse> inventory) {}
}
