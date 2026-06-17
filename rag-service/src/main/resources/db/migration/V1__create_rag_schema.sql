CREATE SCHEMA IF NOT EXISTS rag;

CREATE EXTENSION IF NOT EXISTS vector;

-- Route embeddings — indexed from routing.route-calculated events
CREATE TABLE rag.route_embeddings (
    id              UUID        NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id        VARCHAR(255) NOT NULL UNIQUE,
    shipment_id     VARCHAR(255) NOT NULL,
    origin_city     VARCHAR(255),
    destination_city VARCHAR(255),
    vehicle_type    VARCHAR(100),
    sla_type        VARCHAR(50),
    distance_km     DOUBLE PRECISION NOT NULL DEFAULT 0,
    duration_minutes BIGINT NOT NULL DEFAULT 0,
    fuel_cost_brl   DOUBLE PRECISION NOT NULL DEFAULT 0,
    toll_cost_brl   DOUBLE PRECISION NOT NULL DEFAULT 0,
    total_cost_brl  DOUBLE PRECISION NOT NULL DEFAULT 0,
    embedding       vector(1536),
    indexed_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_route_emb_origin_dest ON rag.route_embeddings(origin_city, destination_city);
CREATE INDEX idx_route_emb_vehicle ON rag.route_embeddings(vehicle_type);

-- Invoice embeddings — indexed from billing.invoice-generated events (waiver + pricing)
CREATE TABLE rag.invoice_embeddings (
    id              UUID        NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id      VARCHAR(255) NOT NULL UNIQUE,
    shipment_id     VARCHAR(255) NOT NULL,
    shipper_id      VARCHAR(255),
    carrier_id      VARCHAR(255),
    origin_city     VARCHAR(255),
    destination_city VARCHAR(255),
    sla_type        VARCHAR(50),
    base_amount_brl DOUBLE PRECISION NOT NULL DEFAULT 0,
    penalty_days    BIGINT NOT NULL DEFAULT 0,
    penalty_amount_brl DOUBLE PRECISION NOT NULL DEFAULT 0,
    total_amount_brl DOUBLE PRECISION NOT NULL DEFAULT 0,
    status          VARCHAR(50),
    waiver_decision VARCHAR(50),
    embedding       vector(1536),
    indexed_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoice_emb_sla ON rag.invoice_embeddings(sla_type);
CREATE INDEX idx_invoice_emb_status ON rag.invoice_embeddings(status);
CREATE INDEX idx_invoice_emb_origin_dest ON rag.invoice_embeddings(origin_city, destination_city);

-- Shipment embeddings — indexed from shipment.created events (demand forecast)
CREATE TABLE rag.shipment_embeddings (
    id              UUID        NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id     VARCHAR(255) NOT NULL UNIQUE,
    shipper_id      VARCHAR(255),
    origin_city     VARCHAR(255),
    destination_city VARCHAR(255),
    sla_type        VARCHAR(50),
    weight_kg       DOUBLE PRECISION NOT NULL DEFAULT 0,
    volume_m3       DOUBLE PRECISION NOT NULL DEFAULT 0,
    requires_hazmat BOOLEAN NOT NULL DEFAULT false,
    requires_cold_chain BOOLEAN NOT NULL DEFAULT false,
    month_key       VARCHAR(7),   -- YYYY-MM for grouping
    embedding       vector(1536),
    indexed_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_shipment_emb_shipper ON rag.shipment_embeddings(shipper_id);
CREATE INDEX idx_shipment_emb_month ON rag.shipment_embeddings(month_key);
CREATE INDEX idx_shipment_emb_origin_dest ON rag.shipment_embeddings(origin_city, destination_city);

-- Inventory embeddings — indexed from warehouse.capacity-updated events
CREATE TABLE rag.inventory_embeddings (
    id              UUID        NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id    VARCHAR(255) NOT NULL UNIQUE,
    warehouse_name  VARCHAR(255),
    location        VARCHAR(500),
    max_weight_kg   DOUBLE PRECISION NOT NULL DEFAULT 0,
    max_volume_m3   DOUBLE PRECISION NOT NULL DEFAULT 0,
    current_weight_kg DOUBLE PRECISION NOT NULL DEFAULT 0,
    current_volume_m3 DOUBLE PRECISION NOT NULL DEFAULT 0,
    utilisation_pct DOUBLE PRECISION NOT NULL DEFAULT 0,
    embedding       vector(1536),
    indexed_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_inventory_emb_utilisation ON rag.inventory_embeddings(utilisation_pct);

-- IVFFlat ANN indexes (cosine distance) — lists=50 as per ADR-024
CREATE INDEX idx_route_ann      ON rag.route_embeddings      USING ivfflat (embedding vector_cosine_ops) WITH (lists = 50);
CREATE INDEX idx_invoice_ann    ON rag.invoice_embeddings    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 50);
CREATE INDEX idx_shipment_ann   ON rag.shipment_embeddings   USING ivfflat (embedding vector_cosine_ops) WITH (lists = 50);
CREATE INDEX idx_inventory_ann  ON rag.inventory_embeddings  USING ivfflat (embedding vector_cosine_ops) WITH (lists = 50);
