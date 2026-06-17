package com.logistics.billing.domain.model;

import java.util.UUID;

public record InvoiceId(UUID value) {
    public InvoiceId { if (value == null) throw new IllegalArgumentException("InvoiceId must not be null"); }
    public static InvoiceId generate() { return new InvoiceId(UUID.randomUUID()); }
    public static InvoiceId of(String uuid) { return new InvoiceId(UUID.fromString(uuid)); }
    @Override public String toString() { return value.toString(); }
}
