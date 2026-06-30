package com.logistics.rag.infrastructure.messaging.dto;

/** Mirrors shipment-service's {@code Address} value object JSON shape (only the city field is consumed). */
public record AddressPayload(String city) {
}
