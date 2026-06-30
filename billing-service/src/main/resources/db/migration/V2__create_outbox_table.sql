-- Transactional Outbox (ADR-030): rows are inserted in the same transaction as the
-- aggregate write, then relayed to Kafka asynchronously by OutboxRelayScheduler.
CREATE TABLE billing.outbox_events (
    id           BIGSERIAL    NOT NULL PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    occurred_at  TIMESTAMPTZ  NOT NULL,
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_events_unpublished ON billing.outbox_events(occurred_at) WHERE published_at IS NULL;
