package com.logistics.rag.infrastructure.messaging.dto;

import java.math.BigDecimal;

/** Mirrors billing-service's {@code Money} value object JSON shape ({amount, currency}). */
public record MoneyPayload(BigDecimal amount, String currency) {
}
