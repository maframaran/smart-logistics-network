# ADR-033 — OSRM Self-Hosted Routing Engine

**Status:** Accepted

---

## Context

`routing-service` calculated road distance using a Haversine straight-line formula (ADR-013). This underestimates real road distance by 20–40% on the São Paulo–Rio corridor, making ETA estimates and fuel/toll cost projections unreliable. The routing port (`RoutingEngine`) was designed from the start as a replaceable adapter — ADR-013 explicitly marked the Haversine implementation as a Phase 4 placeholder.

A real road-routing engine needs to call an external service for turn-by-turn graph data. Options: Google Maps/HERE/Mapbox (paid, external), or OSRM (free, self-hosted, OpenStreetMap data).

## Decision

**Engine:** [OSRM](https://project-osrm.org) `v5.27.1`, self-hosted via Docker using the `osrm/osrm-backend` image with the Multi-Level Dijkstra (MLD) algorithm.

**Data scope:** Southeast Brazil OSM extract from Geofabrik (`sudeste-latest.osm.pbf`) — covers São Paulo and Rio de Janeiro states, which contains all routes used in acceptance tests and demo fixtures. Data is downloaded and preprocessed during `docker build` and baked into the image; no network access to OSM servers is needed at runtime.

**Docker Compose integration:** OSRM runs under the `osrm` profile (`docker compose --profile osrm up --build`). Not included in the default `docker compose up` because the first build takes ~10-15 minutes and 380+ MB of data. The `OSRM_URL` env var is forwarded to routing-service; when blank (default), routing-service uses Haversine.

**`OsrmRoutingEngine`** calls `GET /route/v1/driving/{lon1},{lat1};{lon2},{lat2}?overview=false` via Spring `WebClient` (already a routing-service dependency). Distance (meters) and duration (seconds) are extracted from the first route in the response. Fuel and toll costs remain heuristic (OSRM doesn't provide them), computed from real road distance using the same constants as Haversine.

**Fallback:** `OsrmRoutingEngine` falls back to `HaversineRoutingEngine` on any connectivity error, timeout (5 s), or non-`"Ok"` OSRM response code. This means the system degrades gracefully to straight-line estimates rather than failing a route calculation entirely.

**`RoutingEngineConfig`** selects the active engine bean: when `routing.osrm.url` is blank, the Haversine bean is returned directly; when set, an `OsrmRoutingEngine` wrapping a `WebClient` pointing at that URL is returned. `HaversineRoutingEngine` is no longer `@Component`-annotated — it is always constructed as a Spring bean via the config class, regardless of which engine is active, so it is available as a fallback.

## Alternatives Considered

- **Google Maps Routes API / HERE / Mapbox**: accurate, SLA-backed, but require API keys, incur per-call costs, and introduce external network calls into what is otherwise a fully local dev stack. Rejected — OSRM fits the self-hosted pattern (Postgres, Kafka, Schema Registry all self-hosted) and is free.
- **GraphHopper** (self-hosted, Java): similar capabilities to OSRM, also free. Rejected — OSRM's HTTP API is simpler, single endpoint for routing, and the Docker image is well-maintained.
- **No change / keep Haversine**: acceptable for unit tests and offline dev, but not adequate as a "real" routing implementation for demonstration purposes. Retained as a named fallback, not deleted.

## Consequences

- Real SP→RIO road distance (~440 km) replaces Haversine's underestimate (~360 km). ETA and fuel/toll estimates are more realistic.
- `docker compose --profile osrm up --build` first run is slow (~10-15 min): OSM data download + `osrm-extract` processing. Subsequent starts are fast (data baked into image layer).
- OSRM data reflects OSM at the time the image was last built; data drift is expected. Rebuilding the image refreshes the extract. Cadence: rebuild quarterly or when route data issues are reported.
- When running without `--profile osrm` (the default dev path), `OSRM_URL` is blank and Haversine is used transparently. All unit and acceptance tests run against Haversine — no test requires OSRM to be running.
- The Southeast Brazil extract covers São Paulo + Rio de Janeiro states. Routes to/from other Brazilian states fall outside this bounding box and will return OSRM `"NoRoute"`, causing the engine to fall back to Haversine for those legs.

## Related

- [ADR-013 — Haversine Routing](ADR-013-haversine-routing.md) — superseded by this ADR for production use; retained as the fallback engine.
- Phase 3+4+5 implementation plan — this is Stage 4.
