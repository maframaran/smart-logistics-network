CREATE SCHEMA IF NOT EXISTS billing;

CREATE TABLE billing.invoices (
    id                  UUID            NOT NULL PRIMARY KEY,
    shipment_id         VARCHAR(255)    NOT NULL UNIQUE,
    shipper_id          VARCHAR(255)    NOT NULL,
    carrier_id          VARCHAR(255)    NOT NULL,
    base_amount         NUMERIC(12, 2)  NOT NULL,
    base_currency       VARCHAR(10)     NOT NULL DEFAULT 'BRL',
    penalty_days_late   BIGINT          NOT NULL DEFAULT 0,
    penalty_amount      NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    penalty_currency    VARCHAR(10)     NOT NULL DEFAULT 'BRL',
    total_amount        NUMERIC(12, 2)  NOT NULL,
    total_currency      VARCHAR(10)     NOT NULL DEFAULT 'BRL',
    due_date            DATE            NOT NULL,
    status              VARCHAR(50)     NOT NULL,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoices_shipment ON billing.invoices(shipment_id);
CREATE INDEX idx_invoices_status   ON billing.invoices(status);
CREATE INDEX idx_invoices_carrier  ON billing.invoices(carrier_id);
