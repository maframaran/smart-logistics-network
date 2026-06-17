# Epic: EP-007 — Event-Driven Architecture

**Phase:** 3
**Domain:** Cross-cutting

## Problem

Phase 1 & 2 services may use synchronous integration as a shortcut. Phase 3 formalizes full Kafka-based event-driven integration, ensuring all cross-service communication is async and decoupled.

## Success Metrics

- Zero direct REST calls between services (verified by JPMS `requires` constraints)
- All domain events published to Kafka within 100ms of aggregate state change
- Consumer lag < 1 second under normal load

## Features

- Kafka topic provisioning and governance
- Dead-letter queue handling per topic
- Consumer group management
- Event schema versioning (Avro + Schema Registry)
- Idempotency guarantees for all consumers

## Domain Events Scope

All events defined in `architecture/integration.md`.

## Dependencies

- [ADR-002](../../adrs/ADR-002-kafka.md)
- [ADR-004](../../adrs/ADR-004-jpms.md)
