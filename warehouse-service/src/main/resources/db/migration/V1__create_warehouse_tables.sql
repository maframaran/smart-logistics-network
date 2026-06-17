CREATE SCHEMA IF NOT EXISTS warehouse;

CREATE TABLE warehouse.warehouses (
    id              UUID            NOT NULL PRIMARY KEY,
    name            VARCHAR(500)    NOT NULL,
    location        VARCHAR(500)    NOT NULL,
    max_weight_kg   DOUBLE PRECISION NOT NULL,
    max_volume_m3   DOUBLE PRECISION NOT NULL,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE TABLE warehouse.inventory_items (
    id              UUID            NOT NULL PRIMARY KEY,
    warehouse_id    UUID            NOT NULL REFERENCES warehouse.warehouses(id),
    sku             VARCHAR(100)    NOT NULL,
    description     VARCHAR(1000),
    weight_kg       DOUBLE PRECISION NOT NULL,
    volume_m3       DOUBLE PRECISION NOT NULL,
    quantity        INTEGER         NOT NULL DEFAULT 0,
    UNIQUE (warehouse_id, sku)
);

CREATE INDEX idx_inventory_warehouse ON warehouse.inventory_items(warehouse_id);
