# Epic: EP-008 — Microservices

**Phase:** 3
**Domain:** Cross-cutting / Infrastructure

## Problem

Services need formal REST API contracts, service discovery, health checks, and observability to operate reliably in production.

## Success Metrics

- 99.9% uptime per service
- P99 REST latency < 200ms
- All services expose health/readiness endpoints

## Features

- OpenAPI 3 contract per service
- Health and readiness endpoints (`/actuator/health`)
- Distributed tracing (OpenTelemetry)
- Centralized structured logging (JSON)
- Service-level metrics (Micrometer → Prometheus)

## Dependencies

- [ADR-003](../../adrs/ADR-003-docker.md)
- [ADR-005](../../adrs/ADR-005-hexagonal.md)
