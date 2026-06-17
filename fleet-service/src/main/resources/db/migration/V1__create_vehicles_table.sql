CREATE SCHEMA IF NOT EXISTS fleet;

CREATE TABLE fleet.vehicles (
    id              UUID            NOT NULL PRIMARY KEY,
    license_plate   VARCHAR(50)     NOT NULL UNIQUE,
    type            VARCHAR(50)     NOT NULL,
    max_weight_kg   DOUBLE PRECISION NOT NULL,
    max_volume_m3   DOUBLE PRECISION NOT NULL,
    carrier_id      VARCHAR(255)    NOT NULL,
    status          VARCHAR(50)     NOT NULL,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_vehicles_status     ON fleet.vehicles(status);
CREATE INDEX idx_vehicles_carrier    ON fleet.vehicles(carrier_id);
