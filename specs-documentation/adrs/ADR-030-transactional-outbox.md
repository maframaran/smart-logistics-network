# ADR-030 — Transactional Outbox Pattern

**Status:** Accepted

---

## Context

Every use case that raises a domain event followed the same pattern: `repository.save(aggregate)` then `aggregate.pullDomainEvents().forEach(eventPublisher::publish)`, both inside one `@Transactional` method — confirmed directly in `GenerateInvoiceService`, `RegisterDriverService`, and across all 6 producing services. The risk isn't code ordering — it's that `KafkaTemplate.send()` is **non-blocking**: it returns before broker acknowledgment, so the transaction can commit successfully while the actual publish later fails (broker unreachable, serialization error, network partition). Nothing retried or durably recorded the event in that case; it was silently lost.

This is the first stage built on top of the Stage 1 OpenAPI work and is treated as a cross-cutting correctness fix — later Kafka consumers (predictive maintenance, CQRS projectors, marketplace) are built on top of this pattern, not before it.

## Decision

**Per-service outbox, not extracted to `common`.** Each of the 6 producing services (`shipment`, `fleet`, `driver`, `routing`, `warehouse`, `billing`) gets its own `outbox_events` table (Flyway `V2__create_outbox_table.sql`) in its own schema, its own package-private `OutboxEventEntity`/`OutboxJpaRepositoryPort` in `infrastructure/persistence`, and its own `OutboxRelayScheduler`. This mirrors every other JPA persistence pattern in this codebase, which is already 100% duplicated per service with zero shared infrastructure library — `common` stays a pure domain kernel (`AggregateRoot`, `DomainEvent`) with zero Spring/JPA dependencies.

**Write path.** `XxxJpaRepository.save(aggregate)` now does two things in the same method, with no `@Transactional` of its own (it inherits the calling use case's transaction boundary, confirmed by inspecting the existing repositories before this change): persist the aggregate's JPA entity, then call `aggregate.pullDomainEvents()` and insert one outbox row per event (`aggregate_id`, `event_type` = the event class's simple name, `payload` = `ObjectMapper.writeValueAsString(event)` stored as `TEXT`, `occurred_at`, `published_at` nullable). This moves the pull-and-record step out of the use case and into the repository, so it's atomic with the aggregate write by construction. Every use case's `eventPublisher` field and post-save publish loop were removed — the repository is now the only thing that touches domain events on the write path.

**Relay path.** `OutboxRelayScheduler` (`@Scheduled(fixedDelayString = "${outbox.relay.poll-interval-ms:500}")`, `@EnableScheduling` added to each service's `@SpringBootApplication` class) polls up to 100 unpublished rows ordered by `occurred_at`, deserializes each row's JSON payload back into its concrete `DomainEvent` subtype via a small per-service `Map<String, Class<? extends DomainEvent>>` registry, and calls the **existing, unchanged** `XxxKafkaPublisher.publish(event)` — reusing 100% of the topic-dispatch logic from [ADR-027](ADR-027-kafka-topic-config-dispatch.md) without touching the publisher's internals.

The relay blocks (`.get(10, TimeUnit.SECONDS)`) on each publish before marking `published_at` — the one place in the codebase allowed to wait synchronously on a Kafka send, since the entire point of the relay is to know whether the broker actually acknowledged before declaring the row done. This required changing `publish(DomainEvent event)` on all 6 `XxxEventPublisher` domain ports from `void` to `CompletableFuture<Void>` (a generic JDK type, not a Kafka-specific result type, to keep the domain port free of infrastructure imports), with each `XxxKafkaPublisher` implementation now returning `kafkaTemplate.send(...).thenAccept(result -> {})`. A failed or timed-out publish leaves the row unpublished, logs the failure, and is retried on the next poll — no row is ever marked published without a confirmed broker ack.

**Why `infrastructure/persistence`, not `infrastructure/messaging`, for the scheduler.** The existing convention in this codebase keeps JPA entities and repository-port interfaces package-private (confirmed: `ShipmentJpaEntity`, `ShipmentJpaRepositoryPort`). `OutboxRelayScheduler` needs direct access to `OutboxEventEntity`/`OutboxJpaRepositoryPort` to query and update rows. Rather than widening their visibility to `public` (which would be the only reason to do so) or duplicating query logic through a new abstraction, the scheduler simply lives in the same package. This keeps the package-private convention 100% intact at the cost of a directory name that's slightly broader than its contents — `infrastructure/persistence` now also contains the one piece of infrastructure that talks to Kafka.

## Alternatives Considered

- **Shared `common` outbox library**: rejected — would be the first Spring/JPA dependency in `common`, breaking its role as a pure domain kernel; every other persistence pattern here is duplicated per service anyway.
- **CDC (Debezium reading the WAL)**: rejected as out of scope — adds a new infrastructure component (Debezium + Kafka Connect) for a problem the relay-polling approach solves adequately at this scale; revisit if poll-based relay latency becomes a measured problem.
- **Synchronous `kafkaTemplate.send(...).get()` directly in the use case** (skip the outbox table entirely): rejected — still couples the use case's transaction to Kafka availability; a slow or down broker would block/fail the HTTP request instead of degrading gracefully to "eventually published."

## Consequences

- **At-least-once delivery.** A crash between a confirmed publish and the `published_at` update would cause a duplicate publish on next relay run. No consumer in this codebase is yet verified idempotent — this is an explicit, flagged gap, necessary to close before Stage 6's CQRS projectors are built (they must do idempotent upserts).
- **Eventual publish, not synchronous.** Callers of `create()`/`assign()`/etc. no longer get any signal about whether the Kafka publish succeeded — only that it will be retried until it does. This was already implicitly true (`KafkaTemplate.send()` was non-blocking before this change too), but is now an explicit, documented property instead of an accidental one.
- **New `outbox.relay.poll-interval-ms` config** (default 500ms) per service — not yet exposed in `application.yml`, deliberately left at the code default for this stage; revisit if production tuning needs differ from dev/demo defaults.
- Published outbox rows are kept indefinitely (no cleanup job) — acceptable at this scale; flagged as future work if table growth becomes a real concern.
- Every new aggregate-raising use case from now on must **not** call `eventPublisher.publish()` directly — publishing happens exclusively through `repository.save()` → outbox → relay. Added to `code-creation-guide.md`.

## Related

- [ADR-008 — Aggregate Factories](ADR-008-aggregate-factories.md) (`pullDomainEvents()`)
- [ADR-027 — Kafka Topic Config Dispatch](ADR-027-kafka-topic-config-dispatch.md) (unchanged, reused by the relay)
- [ADR-031 — DLQ + Retry Policy](ADR-031-dlq-retry-policy.md) (companion piece of this same stage, consumer side)
- Phase 3+4+5 implementation plan (see `README.md` Roadmap section) — this is Stage 2
