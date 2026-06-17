CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE notification.notifications (
    id                  UUID            NOT NULL PRIMARY KEY,
    type                VARCHAR(100)    NOT NULL,
    channel             VARCHAR(50)     NOT NULL,
    recipient_address   VARCHAR(500)    NOT NULL,
    recipient_name      VARCHAR(500),
    subject             VARCHAR(1000),
    body                TEXT            NOT NULL,
    reference_id        VARCHAR(255)    NOT NULL,
    status              VARCHAR(50)     NOT NULL,
    failure_reason      TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    version             BIGINT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_notifications_reference ON notification.notifications(reference_id);
CREATE INDEX idx_notifications_status    ON notification.notifications(status);
