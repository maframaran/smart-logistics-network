# ADR-001 — Java 21

**Status:** Accepted

---

## Context

The platform runs multiple services that handle high-concurrency I/O: Kafka consumers, outbound REST calls to Maps/Routing APIs, and database queries. Traditional thread-per-request models under heavy load require large thread pools that consume significant memory.

---

## Decision

Use **Java 21** as the baseline JDK for all services.

Key reasons:
- **Virtual threads (Project Loom — JEP 444):** Virtual threads are cheap to create and block without consuming OS threads. Kafka consumers and REST handlers can block on I/O without thread-pool tuning.
- **Sequenced Collections (JEP 431):** Cleaner API for ordered domain collections (route segments, shipment history).
- **Record patterns and pattern matching (JEP 440/441):** Reduce boilerplate in domain value object handling and event dispatch.
- **LTS release:** Long-term support ensures stability for a production platform.

---

## Consequences

- All services compile with `--release 21`; JPMS `module-info.java` files are mandatory (see [ADR-004](ADR-004-jpms.md))
- Virtual threads are enabled via Spring Boot's `spring.threads.virtual.enabled=true` (Spring Boot 3.2+)
- Minimum Docker base image: `eclipse-temurin:21-jre-alpine`
- Developers must use JDK 21+
