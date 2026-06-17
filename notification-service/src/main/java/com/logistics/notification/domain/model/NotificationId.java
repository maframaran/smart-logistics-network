package com.logistics.notification.domain.model;

import java.util.UUID;

public record NotificationId(UUID value) {
    public NotificationId { if (value == null) throw new IllegalArgumentException("NotificationId must not be null"); }
    public static NotificationId generate() { return new NotificationId(UUID.randomUUID()); }
    public static NotificationId of(String uuid) { return new NotificationId(UUID.fromString(uuid)); }
    @Override public String toString() { return value.toString(); }
}
