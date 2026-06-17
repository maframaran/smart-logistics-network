CREATE SCHEMA IF NOT EXISTS shipment;

CREATE TABLE shipment.shipments (
    id                      UUID            NOT NULL PRIMARY KEY,
    shipper_id              VARCHAR(255)    NOT NULL,

    origin_street           VARCHAR(500)    NOT NULL,
    origin_city             VARCHAR(255)    NOT NULL,
    origin_state            VARCHAR(255),
    origin_postal_code      VARCHAR(50),
    origin_country          VARCHAR(100)    NOT NULL,
    origin_latitude         DOUBLE PRECISION NOT NULL,
    origin_longitude        DOUBLE PRECISION NOT NULL,

    destination_street      VARCHAR(500)    NOT NULL,
    destination_city        VARCHAR(255)    NOT NULL,
    destination_state       VARCHAR(255),
    destination_postal_code VARCHAR(50),
    destination_country     VARCHAR(100)    NOT NULL,
    destination_latitude    DOUBLE PRECISION NOT NULL,
    destination_longitude   DOUBLE PRECISION NOT NULL,

    weight_kg               DOUBLE PRECISION NOT NULL,
    volume_m3               DOUBLE PRECISION NOT NULL,
    requires_hazmat         BOOLEAN          NOT NULL DEFAULT FALSE,
    requires_cold_chain     BOOLEAN          NOT NULL DEFAULT FALSE,

    sla_type                VARCHAR(50)     NOT NULL,
    status                  VARCHAR(50)     NOT NULL,
    required_delivery_date  DATE            NOT NULL,

    assigned_vehicle_id     VARCHAR(255),
    assigned_driver_id      VARCHAR(255),
    route_id                VARCHAR(255),

    version                 BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_shipments_status    ON shipment.shipments(status);
CREATE INDEX idx_shipments_shipper   ON shipment.shipments(shipper_id);
