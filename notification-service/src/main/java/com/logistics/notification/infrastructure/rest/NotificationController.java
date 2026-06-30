package com.logistics.notification.infrastructure.rest;

import com.logistics.notification.domain.model.*;
import com.logistics.notification.domain.ports.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Tag(name = "Notifications", description = "Email delivery via Kafka event consumption")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SendNotificationUseCase sendNotification;
    private final GetNotificationUseCase getNotification;

    public NotificationController(SendNotificationUseCase sendNotification, GetNotificationUseCase getNotification) {
        this.sendNotification = sendNotification;
        this.getNotification = getNotification;
    }

    @Operation(summary = "Send a notification")
    @ApiResponse(responseCode = "201", description = "Notification queued")
    @PostMapping
    public ResponseEntity<NotificationResponse> send(@RequestBody SendNotificationRequest request) {
        NotificationId id = sendNotification.send(new SendNotificationUseCase.Command(
                NotificationType.valueOf(request.type()),
                NotificationChannel.valueOf(request.channel()),
                request.recipientAddress(), request.recipientName(),
                request.subject(), request.body(), request.referenceId()
        ));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(new NotificationResponse(id.toString()));
    }

    @Operation(summary = "Get a notification by ID")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDetailResponse> get(@PathVariable String id) {
        Notification n = getNotification.findById(NotificationId.of(id));
        return ResponseEntity.ok(toDetail(n));
    }

    @Operation(summary = "List notifications", description = "Filter by referenceId (shipmentId/invoiceId) or status (defaults to PENDING).")
    @GetMapping
    public ResponseEntity<List<NotificationDetailResponse>> list(
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String status
    ) {
        List<Notification> results;
        if (referenceId != null) {
            results = getNotification.findByReferenceId(referenceId);
        } else {
            String effectiveStatus = status != null ? status : "PENDING";
            results = getNotification.findByStatus(NotificationStatus.valueOf(effectiveStatus));
        }
        return ResponseEntity.ok(results.stream().map(this::toDetail).toList());
    }

    private NotificationDetailResponse toDetail(Notification n) {
        return new NotificationDetailResponse(n.getId().toString(), n.getType().name(), n.getChannel().name(),
                n.getRecipientAddress(), n.getReferenceId(), n.getStatus().name(), n.getCreatedAt());
    }

    record SendNotificationRequest(String type, String channel, String recipientAddress,
                                    String recipientName, String subject, String body, String referenceId) {}
    record NotificationResponse(String notificationId) {}
    record NotificationDetailResponse(String notificationId, String type, String channel,
                                       String recipientAddress, String referenceId, String status, Instant createdAt) {}
}
