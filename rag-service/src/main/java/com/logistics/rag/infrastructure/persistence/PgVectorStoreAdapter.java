package com.logistics.rag.infrastructure.persistence;

import com.logistics.rag.domain.model.InventoryMetadata;
import com.logistics.rag.domain.model.InventoryRow;
import com.logistics.rag.domain.model.InvoiceMetadata;
import com.logistics.rag.domain.model.InvoiceSearchRow;
import com.logistics.rag.domain.model.RouteMetadata;
import com.logistics.rag.domain.model.RouteSearchRow;
import com.logistics.rag.domain.model.ShipmentMetadata;
import com.logistics.rag.domain.model.ShipmentSearchRow;
import com.logistics.rag.domain.ports.out.VectorStorePort;
import com.pgvector.PGvector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * Binds embeddings via the {@code com.pgvector:pgvector} JDBC type ({@link PGvector}, a
 * {@code PGobject}) rather than PostgreSQL string literals, per ADR-024. The {@code vector}
 * type must be registered per-{@link java.sql.Connection}, so the injected (pooled) DataSource
 * is wrapped in {@link VectorAwareDataSource} to register it on every checkout.
 */
@Component
public class PgVectorStoreAdapter implements VectorStorePort {

    private static final String COL_SHIPMENT_ID = "shipment_id";
    private static final String COL_ORIGIN_CITY = "origin_city";
    private static final String COL_DESTINATION_CITY = "destination_city";
    private static final String COL_SLA_TYPE = "sla_type";
    private static final String COL_SIMILARITY = "similarity";

    private final JdbcTemplate jdbc;

    public PgVectorStoreAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(new VectorAwareDataSource(dataSource));
    }

    // ── Upserts ──────────────────────────────────────────────────────────────

    @Override
    public void upsertRoute(String routeId, float[] embedding, RouteMetadata m) {
        jdbc.update("""
            INSERT INTO rag.route_embeddings
              (route_id, shipment_id, origin_city, destination_city, vehicle_type, sla_type,
               distance_km, duration_minutes, fuel_cost_brl, toll_cost_brl, total_cost_brl, embedding)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT (route_id) DO UPDATE SET
              distance_km = EXCLUDED.distance_km,
              duration_minutes = EXCLUDED.duration_minutes,
              fuel_cost_brl = EXCLUDED.fuel_cost_brl,
              toll_cost_brl = EXCLUDED.toll_cost_brl,
              total_cost_brl = EXCLUDED.total_cost_brl,
              embedding = EXCLUDED.embedding,
              indexed_at = now()
            """,
                routeId, m.shipmentId(), m.originCity(), m.destinationCity(),
                m.vehicleType(), m.slaType(),
                m.distanceKm(), m.durationMinutes(),
                m.fuelCostBrl(), m.tollCostBrl(), m.totalCostBrl(),
                new PGvector(embedding));
    }

    @Override
    public void upsertInvoice(String invoiceId, float[] embedding, InvoiceMetadata m) {
        jdbc.update("""
            INSERT INTO rag.invoice_embeddings
              (invoice_id, shipment_id, shipper_id, carrier_id, origin_city, destination_city,
               sla_type, base_amount_brl, penalty_days, penalty_amount_brl, total_amount_brl, status, embedding)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT (invoice_id) DO UPDATE SET
              status = EXCLUDED.status,
              penalty_days = EXCLUDED.penalty_days,
              penalty_amount_brl = EXCLUDED.penalty_amount_brl,
              total_amount_brl = EXCLUDED.total_amount_brl,
              embedding = EXCLUDED.embedding,
              indexed_at = now()
            """,
                invoiceId, m.shipmentId(), m.shipperId(), m.carrierId(),
                m.originCity(), m.destinationCity(), m.slaType(),
                m.baseAmountBrl(), m.penaltyDays(), m.penaltyAmountBrl(),
                m.totalAmountBrl(), m.status(),
                new PGvector(embedding));
    }

    @Override
    public void upsertShipment(String shipmentId, float[] embedding, ShipmentMetadata m) {
        jdbc.update("""
            INSERT INTO rag.shipment_embeddings
              (shipment_id, shipper_id, origin_city, destination_city, sla_type,
               weight_kg, volume_m3, requires_hazmat, requires_cold_chain, month_key, embedding)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT (shipment_id) DO UPDATE SET
              month_key = EXCLUDED.month_key,
              embedding = EXCLUDED.embedding,
              indexed_at = now()
            """,
                shipmentId, m.shipperId(), m.originCity(), m.destinationCity(),
                m.slaType(), m.weightKg(), m.volumeM3(),
                m.requiresHazmat(), m.requiresColdChain(), m.monthKey(),
                new PGvector(embedding));
    }

    @Override
    public void upsertInventory(String warehouseId, float[] embedding, InventoryMetadata m) {
        jdbc.update("""
            INSERT INTO rag.inventory_embeddings
              (warehouse_id, warehouse_name, location,
               max_weight_kg, max_volume_m3, current_weight_kg, current_volume_m3, utilisation_pct, embedding)
            VALUES (?,?,?,?,?,?,?,?,?)
            ON CONFLICT (warehouse_id) DO UPDATE SET
              current_weight_kg = EXCLUDED.current_weight_kg,
              current_volume_m3 = EXCLUDED.current_volume_m3,
              utilisation_pct = EXCLUDED.utilisation_pct,
              embedding = EXCLUDED.embedding,
              indexed_at = now()
            """,
                warehouseId, m.warehouseName(), m.location(),
                m.maxWeightKg(), m.maxVolumeM3(),
                m.currentWeightKg(), m.currentVolumeM3(), m.utilisationPct(),
                new PGvector(embedding));
    }

    // ── ANN queries ─────────────────────────────────────────────────────────

    @Override
    public List<RouteSearchRow> findSimilarRoutes(float[] queryEmbedding, int topK) {
        PGvector vec = new PGvector(queryEmbedding);
        return jdbc.query("""
            SELECT route_id, shipment_id, origin_city, destination_city, vehicle_type, sla_type,
                   distance_km, duration_minutes, fuel_cost_brl, toll_cost_brl, total_cost_brl,
                   1 - (embedding <=> ?) AS similarity
            FROM rag.route_embeddings
            ORDER BY embedding <=> ?
            LIMIT ?
            """,
                (rs, rowNum) -> new RouteSearchRow(
                        rs.getString("route_id"), rs.getString(COL_SHIPMENT_ID),
                        rs.getString(COL_ORIGIN_CITY), rs.getString(COL_DESTINATION_CITY),
                        rs.getString("vehicle_type"), rs.getString(COL_SLA_TYPE),
                        rs.getDouble("distance_km"), rs.getLong("duration_minutes"),
                        rs.getDouble("fuel_cost_brl"), rs.getDouble("toll_cost_brl"), rs.getDouble("total_cost_brl"),
                        rs.getDouble(COL_SIMILARITY)),
                vec, vec, topK);
    }

    @Override
    public List<InvoiceSearchRow> findSimilarInvoices(float[] queryEmbedding, int topK) {
        PGvector vec = new PGvector(queryEmbedding);
        return jdbc.query("""
            SELECT invoice_id, shipment_id, shipper_id, carrier_id, origin_city, destination_city,
                   sla_type, base_amount_brl, penalty_days, penalty_amount_brl, total_amount_brl,
                   status, waiver_decision,
                   1 - (embedding <=> ?) AS similarity
            FROM rag.invoice_embeddings
            WHERE status = 'PAID'
            ORDER BY embedding <=> ?
            LIMIT ?
            """,
                (rs, rowNum) -> new InvoiceSearchRow(
                        rs.getString("invoice_id"), rs.getString(COL_SHIPMENT_ID),
                        rs.getString("shipper_id"), rs.getString("carrier_id"),
                        rs.getString(COL_ORIGIN_CITY), rs.getString(COL_DESTINATION_CITY), rs.getString(COL_SLA_TYPE),
                        rs.getDouble("base_amount_brl"), rs.getLong("penalty_days"),
                        rs.getDouble("penalty_amount_brl"), rs.getDouble("total_amount_brl"),
                        rs.getString("status"), rs.getString("waiver_decision"),
                        rs.getDouble(COL_SIMILARITY)),
                vec, vec, topK);
    }

    @Override
    public List<ShipmentSearchRow> findSimilarShipments(float[] queryEmbedding, int topK) {
        PGvector vec = new PGvector(queryEmbedding);
        return jdbc.query("""
            SELECT shipment_id, shipper_id, origin_city, destination_city, sla_type,
                   weight_kg, volume_m3, month_key,
                   1 - (embedding <=> ?) AS similarity
            FROM rag.shipment_embeddings
            ORDER BY embedding <=> ?
            LIMIT ?
            """,
                (rs, rowNum) -> new ShipmentSearchRow(
                        rs.getString(COL_SHIPMENT_ID), rs.getString("shipper_id"),
                        rs.getString(COL_ORIGIN_CITY), rs.getString(COL_DESTINATION_CITY), rs.getString(COL_SLA_TYPE),
                        rs.getDouble("weight_kg"), rs.getDouble("volume_m3"), rs.getString("month_key"),
                        rs.getDouble(COL_SIMILARITY)),
                vec, vec, topK);
    }

    @Override
    public List<InventoryRow> findAllInventory() {
        return jdbc.query("""
            SELECT warehouse_id, warehouse_name, location,
                   max_weight_kg, max_volume_m3, current_weight_kg, current_volume_m3, utilisation_pct
            FROM rag.inventory_embeddings
            ORDER BY utilisation_pct DESC
            """,
                (rs, rowNum) -> new InventoryRow(
                        rs.getString("warehouse_id"), rs.getString("warehouse_name"), rs.getString("location"),
                        rs.getDouble("max_weight_kg"), rs.getDouble("max_volume_m3"),
                        rs.getDouble("current_weight_kg"), rs.getDouble("current_volume_m3"),
                        rs.getDouble("utilisation_pct")));
    }
}
