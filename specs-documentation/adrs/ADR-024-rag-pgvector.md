# ADR-024 — RAG with pgvector and Claude API

**Date:** 2026-06-17
**Status:** Accepted
**Deciders:** Platform Architecture Team

---

## Context

Phase 4 introduces AI capabilities across five use cases: route cost estimation, SLA penalty waiver decisions, dynamic pricing recommendations, warehouse inventory rebalancing, and demand forecasting. All five require retrieval of semantically similar historical records before calling a language model — the Retrieval-Augmented Generation (RAG) pattern.

The key infrastructure decisions are: (1) which vector store to use, and (2) which model provider.

---

## Decision

**Vector store:** `pgvector` extension on the existing PostgreSQL 16 instance.

**Embeddings + completions:** Anthropic Claude API — `claude-haiku-4-5-20251001` for embedding generation, `claude-sonnet-4-6` for completions.

All five use cases are implemented in a single new `rag-service` Maven module (port 8088) following the same hexagonal architecture as every other service.

---

## Alternatives Considered

| Option | Rejected because |
|--------|-----------------|
| **Weaviate** | New stateful container to operate and monitor; schema management outside Flyway |
| **Pinecone** | External SaaS dependency; data leaves the platform; cost at scale |
| **ChromaDB** | Python-native; requires polyglot infra and separate deployment |
| **Qdrant** | Excellent performance but same operational burden as Weaviate |
| **OpenAI embeddings** | Different vendor; adds `text-embedding-3-small` API contract alongside existing Claude calls |

---

## Consequences

### pgvector

- Zero new infrastructure — the extension is installed via Flyway migration (`CREATE EXTENSION IF NOT EXISTS vector`)
- Schema managed alongside all other schemas under the `rag` Flyway namespace
- IVFFlat indexes (`lists = 50`) provide ANN search; exact recall for < 10 K rows; approximate for larger datasets
- `vector(1536)` column type; cosine distance via `<=>` operator
- `ON CONFLICT DO UPDATE` upserts keep embeddings current as domain data changes

### Claude API

- Single vendor for both embeddings and completions — one API key, one billing relationship
- `claude-haiku-4-5-20251001` for bulk embedding (fast, cheap) — embedding text built from structured domain fields; not raw user input
- `claude-sonnet-4-6` for recommendations — structured JSON output via tool_use response block
- `ANTHROPIC_API_KEY` injected via environment variable; never committed

### Operational

- pgvector adds ~5 MB to the PostgreSQL image; no performance impact on existing schemas (separate `rag` schema)
- If Claude API is unreachable, all five RAG endpoints return 503; billing-service falls back to static pricing
- Kafka consumers (4 topics) keep embeddings eventually consistent; lag monitored via standard consumer group metrics

---

## Implementation Notes

- Java type for pgvector: `com.pgvector:pgvector:0.1.6` — registered as `PGobject` via `JdbcTemplate`
- ANN query pattern: `SELECT … FROM rag.<table> ORDER BY embedding <=> $1::vector LIMIT ?`
- Embedding text is a deterministic plain-text string built from structured fields (not free text); reproducible for re-indexing
- Re-indexing triggered by replaying Kafka topic from offset 0; no separate backfill job needed

---

## Related

- [EP-018](../specs/epics/EP-018-rag-intelligence.md) — RAG Intelligence epic
- [ADR-002](ADR-002-kafka.md) — Kafka for event streaming (source of indexing events)
- [ADR-003](ADR-003-docker.md) — Docker Compose (pgvector enabled in existing postgres service)
- [ADR-018](ADR-018-bigdecimal-money.md) — Money values passed as plain strings to embedding text
