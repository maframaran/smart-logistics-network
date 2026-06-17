# Epic: EP-009 — CQRS

**Phase:** 3
**Domain:** Cross-cutting

## Problem

Query patterns (shipment tracking, fleet dashboard, billing reports) put read load on the same models used for writes, causing performance degradation under heavy traffic.

## Success Metrics

- Read queries served in < 50ms (P99) from read models
- Write throughput unaffected by read query load
- Read models eventually consistent within < 2 seconds of write

## Features

- Separate read models (projections) per domain, built from Kafka events
- Command/Query split at the use case port level
- Read model storage optimized per query pattern (denormalized views)

## Dependencies

- [EP-007](EP-007-event-driven-architecture.md) — Kafka events feed read model projections
- [ADR-006](../../adrs/ADR-006-ddd.md)
