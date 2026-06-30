# ADR-031 — Dead Letter Queue + Retry Policy

**Status:** Accepted

---

## Context

The two services with `@KafkaListener` consumers had no real error handling. `notification-service`'s `ShipmentEventConsumer` had **zero** error handling — any exception (a malformed payload, a downstream failure) would be handled by Spring Kafka's default behavior, which is to log and keep retrying the same record indefinitely, blocking that partition. `rag-service`'s `RagKafkaConsumer` was worse in a different way: every listener method wrapped its body in `try { ... } catch (Exception e) { log.error(...); }` — silently swallowing the failure entirely, so a poison message or transient LLM/DB failure just vanished with no retry and no record.

## Decision

Add one `KafkaConsumerConfig` per consuming service (`notification-service`, `rag-service`) exposing a `DefaultErrorHandler` bean. Spring Boot's auto-configured `ConcurrentKafkaListenerContainerFactory` picks up this bean automatically and applies it to every `@KafkaListener` in the service — no per-listener wiring needed.

**Retry policy**: `ExponentialBackOff(1000L, 2.0)` (1s initial interval, ×2 multiplier) capped at `maxInterval = 10_000L` (10s) and `maxElapsedTime = 31_000L` (~3 retries total within the 1s→10s envelope: 1s, 2s, 4s, ... capped, until ~31s elapsed).

**Dead letter routing**: a `DeadLetterPublishingRecoverer` is invoked once the backoff is exhausted, publishing the failed record to `<original-topic>.DLT` (e.g. `shipment.created.DLT`) via a `BiFunction<ConsumerRecord<?,?>, Exception, TopicPartition>` that appends the `.DLT` suffix and preserves the original partition. This is the same dead-letter topic naming Spring Kafka's own `DeadLetterPublishingRecoverer` defaults to when constructed with the simpler constructor, made explicit here so the behavior is documented rather than implicit.

As part of this stage, `RagKafkaConsumer`'s per-listener `try/catch` blocks were removed — exceptions now propagate to the container's error handler, which manages retry/DLQ instead of the listener swallowing them.

## Alternatives Considered

- **Fixed-delay retry** (`FixedBackOff`): rejected — exponential backoff is the better default for transient failures (broker hiccups, momentary downstream unavailability) without hammering a struggling dependency on every retry.
- **Infinite retry** (Spring Kafka's pre-this-change default): rejected — a genuinely poison message (bad payload, permanently broken downstream call) would block the partition forever instead of being set aside for inspection.
- **No DLQ, just log-and-skip**: rejected — loses the failed record entirely with no way to inspect or replay it; the whole point of a DLQ is to make failures visible and recoverable instead of silent.

## Consequences

- Failed records land on `<topic>.DLT` instead of blocking a partition or vanishing silently. **No replay tooling is built in this stage** — inspecting and reprocessing `.DLT` topics today means manually consuming them (e.g. via `kafka-console-consumer`) and replaying by hand. Flagged as future work if DLQ volume in practice makes that workflow inadequate.
- The retry envelope (~31s total) means a listener method can now block its partition for up to ~31 seconds on a failing record before giving up — acceptable at this scale; revisit if a slower downstream dependency needs a longer window.
- New `@KafkaListener` methods in either service automatically inherit this error handling — no extra wiring required, since it's wired at the container-factory level via the `DefaultErrorHandler` bean.
- `shipment-service`, `fleet-service`, `driver-service`, `routing-service`, `warehouse-service`, and `billing-service` are outbox **producers** only ([ADR-030](ADR-030-transactional-outbox.md)) — they have no `@KafkaListener`s today, so this DLQ policy doesn't yet apply to them. Add the same `KafkaConsumerConfig` pattern to any of them the moment they gain a consumer (e.g. `shipment-service` consuming `marketplace.bid-awarded` in the planned Stage 8).

## Related

- [ADR-026 — Typed Records Over Maps](ADR-026-typed-records-over-maps.md) (the `@Payload` DTOs these listeners deserialize into)
- [ADR-030 — Transactional Outbox](ADR-030-transactional-outbox.md) (companion piece of this same stage, producer side)
- Phase 3+4+5 implementation plan (see `README.md` Roadmap section) — this is Stage 2
