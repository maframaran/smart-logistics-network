# ADR-013 — Haversine In-Process Routing Engine (Phase 4 Placeholder)

**Status:** Superseded by [ADR-033 — OSRM Self-Hosted Routing Engine](ADR-033-osrm-routing-engine.md)

`HaversineRoutingEngine` is retained as a fallback engine used when OSRM is unavailable (no `OSRM_URL` configured, or OSRM unreachable). It is no longer the primary production engine.

---

## Context

Route calculation requires distance, ETA, fuel cost, and toll estimates. The production solution is a third-party Maps API (Google Maps, HERE, etc.). However, integrating an external API in Phase 1/2 introduces cost, rate limits, and external dependencies before the core domain is stable.

---

## Decision

`routing-service` ships a `HaversineRoutingEngine` that implements the `RoutingEngine` outbound port using the **Haversine formula** for great-circle distance between two coordinates. It uses fixed constants:

| Constant | Value |
|----------|-------|
| Average speed | 80 km/h |
| Fuel consumption | 0.12 L/km |
| Fuel price | BRL 6.20/L |
| Toll estimate | BRL 0.05/km |

The route is a single direct segment (origin → destination). ETA is `distance / avgSpeed`.

The `RoutingEngine` interface is defined as a **domain outbound port** — the application layer calls the port, never the concrete class. In Phase 4, `HaversineRoutingEngine` is replaced by a `MapsApiRoutingEngine` adapter without touching the domain or application layers.

---

## Consequences

- No external dependencies or costs during development
- Results are approximate (straight-line, no traffic, no road network) — acceptable for Phase 1/2 demos and tests
- The port abstraction means Phase 4 replacement is a single new class + configuration change
- Constants are hardcoded; they should move to configuration (`application.yml`) before production use
