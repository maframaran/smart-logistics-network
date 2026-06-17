# ADR-003 — Docker and Docker Compose

**Status:** Accepted

---

## Context

The platform consists of multiple independent services plus infrastructure dependencies (Kafka, PostgreSQL). Developers need a consistent, reproducible local environment without manually installing and configuring each component.

---

## Decision

Each service is packaged as a **Docker image** and the full local environment is orchestrated with **Docker Compose**.

Structure:
- One `Dockerfile` per service module (multi-stage build: build with JDK 21, run with JRE 21 Alpine)
- One root `docker-compose.yml` bringing up all services + Kafka (KRaft) + PostgreSQL
- Each service gets its own PostgreSQL schema (logical isolation; physical separation planned for production)

Multi-stage Dockerfile pattern:
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
# compile and package

FROM eclipse-temurin:21-jre-alpine
# copy jar, expose port, run
```

---

## Consequences

- Developers run `docker compose up` to start the full platform locally
- CI/CD builds and pushes images tagged with git commit SHA
- Each service container mounts no shared volumes (stateless application layer)
- Database migrations run via Flyway at service startup
- Kafka in KRaft mode (no ZooKeeper container needed) — see [ADR-002](ADR-002-kafka.md)
- Image sizes stay small by using Alpine JRE and excluding test dependencies
