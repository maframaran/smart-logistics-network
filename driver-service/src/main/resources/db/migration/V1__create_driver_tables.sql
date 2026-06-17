CREATE SCHEMA IF NOT EXISTS driver;

CREATE TABLE driver.drivers (
    id                      UUID            NOT NULL PRIMARY KEY,
    full_name               VARCHAR(500)    NOT NULL,
    license_number          VARCHAR(100)    NOT NULL UNIQUE,
    license_class           VARCHAR(10)     NOT NULL,
    hazmaterial_certified   BOOLEAN         NOT NULL DEFAULT FALSE,
    carrier_id              VARCHAR(255)    NOT NULL,
    status                  VARCHAR(50)     NOT NULL,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE TABLE driver.driving_sessions (
    id                      BIGSERIAL       NOT NULL PRIMARY KEY,
    driver_id               UUID            NOT NULL REFERENCES driver.drivers(id),
    date                    DATE            NOT NULL,
    hours_worked_minutes    BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (driver_id, date)
);

CREATE INDEX idx_drivers_status    ON driver.drivers(status);
CREATE INDEX idx_drivers_carrier   ON driver.drivers(carrier_id);
CREATE INDEX idx_sessions_driver   ON driver.driving_sessions(driver_id);
