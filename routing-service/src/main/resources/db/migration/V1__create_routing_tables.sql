CREATE SCHEMA IF NOT EXISTS routing;

CREATE TABLE routing.routes (
    id                      UUID            NOT NULL PRIMARY KEY,
    shipment_id             VARCHAR(255)    NOT NULL UNIQUE,
    vehicle_type            VARCHAR(50)     NOT NULL,
    origin_latitude         DOUBLE PRECISION NOT NULL,
    origin_longitude        DOUBLE PRECISION NOT NULL,
    destination_latitude    DOUBLE PRECISION NOT NULL,
    destination_longitude   DOUBLE PRECISION NOT NULL,
    total_distance_km       DOUBLE PRECISION NOT NULL,
    total_duration_minutes  BIGINT          NOT NULL,
    estimated_arrival       TIMESTAMPTZ     NOT NULL,
    fuel_litres             DOUBLE PRECISION NOT NULL,
    fuel_cost_brl           DOUBLE PRECISION NOT NULL,
    tolls_cost_brl          DOUBLE PRECISION NOT NULL DEFAULT 0,
    status                  VARCHAR(50)     NOT NULL,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE TABLE routing.route_segments (
    id                          BIGSERIAL       NOT NULL PRIMARY KEY,
    route_id                    UUID            NOT NULL REFERENCES routing.routes(id),
    segment_order               INTEGER         NOT NULL,
    label                       VARCHAR(500),
    from_latitude               DOUBLE PRECISION NOT NULL,
    from_longitude              DOUBLE PRECISION NOT NULL,
    to_latitude                 DOUBLE PRECISION NOT NULL,
    to_longitude                DOUBLE PRECISION NOT NULL,
    distance_km                 DOUBLE PRECISION NOT NULL,
    estimated_duration_minutes  BIGINT          NOT NULL
);

CREATE INDEX idx_routes_shipment   ON routing.routes(shipment_id);
CREATE INDEX idx_segments_route    ON routing.route_segments(route_id);
