package com.logistics.driver.infrastructure.rest;

import com.logistics.driver.domain.model.*;
import com.logistics.driver.domain.ports.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Drivers", description = "Driver management and BR-005 hour tracking")
@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final RegisterDriverUseCase registerDriver;
    private final UpdateDriverStatusUseCase updateStatus;
    private final GetDriverUseCase getDriver;
    private final CheckDriverHoursUseCase checkHours;

    public DriverController(RegisterDriverUseCase registerDriver, UpdateDriverStatusUseCase updateStatus,
                            GetDriverUseCase getDriver, CheckDriverHoursUseCase checkHours) {
        this.registerDriver = registerDriver;
        this.updateStatus = updateStatus;
        this.getDriver = getDriver;
        this.checkHours = checkHours;
    }

    @Operation(summary = "Register a driver")
    @ApiResponse(responseCode = "201", description = "Driver registered")
    @PostMapping
    public ResponseEntity<DriverResponse> register(@RequestBody RegisterDriverRequest request) {
        DriverId id = registerDriver.register(new RegisterDriverUseCase.Command(
                request.fullName(), request.licenseNumber(),
                LicenseClass.valueOf(request.licenseClass()),
                request.hazmaterialCertified(), request.carrierId()
        ));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(new DriverResponse(id.toString(), "AVAILABLE"));
    }

    @Operation(summary = "Get a driver by ID")
    @GetMapping("/{id}")
    public ResponseEntity<DriverDetailResponse> get(@PathVariable String id) {
        Driver d = getDriver.findById(DriverId.of(id));
        return ResponseEntity.ok(toDetail(d));
    }

    @Operation(summary = "List drivers", description = "Filter by status, or list available drivers (optionally requiring hazmat certification).")
    @GetMapping
    public ResponseEntity<List<DriverDetailResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") boolean requiresHazmat
    ) {
        List<Driver> drivers = (status != null)
                ? getDriver.findByStatus(DriverStatus.valueOf(status))
                : getDriver.findAvailableDrivers(requiresHazmat);
        return ResponseEntity.ok(drivers.stream().map(this::toDetail).toList());
    }

    @Operation(summary = "Update a driver's status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest request) {
        updateStatus.update(new UpdateDriverStatusUseCase.Command(
                DriverId.of(id), DriverStatus.valueOf(request.status()), request.reason()
        ));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check BR-005 daily driving hours", description = "Returns whether the driver can take on additionalMinutes more driving on the given date without exceeding the 9h/day limit.")
    @GetMapping("/{id}/hours-check")
    public ResponseEntity<HoursCheckResponse> checkHours(
            @PathVariable String id,
            @RequestParam LocalDate date,
            @RequestParam long additionalMinutes
    ) {
        boolean canWork = checkHours.canWork(DriverId.of(id), date, Duration.ofMinutes(additionalMinutes));
        return ResponseEntity.ok(new HoursCheckResponse(canWork));
    }

    private DriverDetailResponse toDetail(Driver d) {
        return new DriverDetailResponse(d.getId().toString(), d.getFullName(), d.getLicenseNumber(),
                d.getLicenseClass().name(), d.isHazmaterialCertified(), d.getCarrierId(), d.getStatus().name());
    }

    record RegisterDriverRequest(String fullName, String licenseNumber, String licenseClass,
                                 boolean hazmaterialCertified, String carrierId) {}
    record UpdateStatusRequest(String status, String reason) {}
    record DriverResponse(String driverId, String status) {}
    record DriverDetailResponse(String driverId, String fullName, String licenseNumber, String licenseClass,
                                boolean hazmaterialCertified, String carrierId, String status) {}
    record HoursCheckResponse(boolean canWork) {}
}
