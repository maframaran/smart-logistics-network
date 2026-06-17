# ADR-014 — Strategy Pattern for Notification Senders

**Status:** Accepted

---

## Context

Notification delivery currently targets only the EMAIL channel. Future phases add SMS, PUSH, and in-app channels. Implementing channel logic with `if/switch` inside the use case makes adding new channels a modification of existing code (violates Open/Closed Principle).

---

## Decision

`notification-service` uses the **Strategy pattern** for channel dispatch:

```java
public interface NotificationSender {
    boolean supports(NotificationChannel channel);
    void send(Notification notification);
}
```

`SendNotificationService` receives `List<NotificationSender>` via constructor injection and selects the first sender that `supports()` the requested channel:

```java
senders.stream()
    .filter(s -> s.supports(channel))
    .findFirst()
    .orElseThrow(() -> new IllegalArgumentException("No sender for channel: " + channel));
```

Currently implemented: `EmailNotificationSender` (uses Spring's `JavaMailSender` via `spring-context-support`).

Adding a new channel (e.g., SMS) requires only a new `@Component` implementing `NotificationSender` — no changes to `SendNotificationService`.

---

## Consequences

- Open to extension, closed to modification for new delivery channels
- Each sender is independently testable with a mock `Notification`
- `SendNotificationService` does not depend on any concrete channel implementation — only the `NotificationSender` port
- Failure isolation: if the selected sender throws, the service calls `notification.markFailed(reason)` and saves the failure — no exception propagates to the Kafka consumer
