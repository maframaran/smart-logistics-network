package com.logistics.rag.infrastructure.persistence;

import com.logistics.rag.domain.ports.out.VectorStorePort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Passes vector values as PostgreSQL string literals ('[f1,f2,...]'::vector).
 * This avoids a compile-time dependency on the pgvector JDBC library while
 * keeping full ANN query support via the native <=> cosine operator.
 */
@Component
public class PgVectorStoreAdapter implements VectorStorePort {

    private final JdbcTemplate jdbc;

    public PgVectorStoreAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    // ── Upserts ──────────────────────────────────────────────────────────────

    @Override
    public void upsertRoute(String routeId, float[] embedding, Map<String, Object> m) {
        jdbc.update("""
            INSERT INTO rag.route_embeddings
              (route_id, shipment_id, origin_city, destination_city, vehicle_type, sla_type,
               distance_km, duration_minutes, fuel_cost_brl, toll_cost_brl, total_cost_brl, embedding)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?::vector)
            ON CONFLICT (route_id) DO UPDATE SET
              distance_km = EXCLUDED.distance_km,
              duration_minutes = EXCLUDED.duration_minutes,
              fuel_cost_brl = EXCLUDED.fuel_cost_brl,
              toll_cost_brl = EXCLUDED.toll_cost_brl,
              total_cost_brl = EXCLUDED.total_cost_brl,
              embedding = EXCLUDED.embedding,
              indexed_at = now()
            """,
                routeId, m.get("shipmentId"), m.get("originCity"), m.get("destinationCity"),
                m.get("vehicleType"), m.get("slaType"),
                m.get("distanceKm"), m.get("durationMinutes"),
                m.get("fuelCostBrl"), m.get("tollCostBrl"), m.get("totalCostBrl"),
                vectorLiteral(embedding));
    }

    @Override
    public void upsertInvoice(String invoiceId, float[] embedding, Map<String, Object> m) {
        jdbc.update("""
            INSERT INTO rag.invoice_embeddings
              (invoice_id, shipment_id, shipper_id, carrier_id, origin_city, destination_city,
               sla_type, base_amount_brl, penalty_days, penalty_amount_brl, total_amount_brl, status, embedding)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?::vector)
            ON CONFLICT (invoice_id) DO UPDATE SET
              status = EXCLUDED.status,
              penalty_days = EXCLUDED.penalty_days,
              penalty_amount_brl = EXCLUDED.penalty_amount_brl,
              total_amount_brl = EXCLUDED.total_amount_brl,
              embedding = EXCLUDED.embedding,
              indexed_at = now()
            """,
                invoiceId, m.get("shipmentId"), m.get("shipperId"), m.get("carrierId"),
                m.get("originCity"), m.get("destinationCity"), m.get("slaType"),
                m.get("baseAmountBrl"), m.get("penaltyDays"), m.get("penaltyAmountBrl"),
                m.get("totalAmountBrl"), m.get("status"),
                vectorLiteral(embedding));
    }

    @Override
    public void upsertShipment(String shipmentId, float[] embedding, Map<String, Object> m) {
        jdbc.update("""
            INSERT INTO rag.shipment_embeddings
              (shipment_id, shipper_id, origin_city, destination_city, sla_type,
               weight_kg, volume_m3, requires_hazmat, requires_cold_chain, month_key, embedding)
            VALUES (?,?,?,?,?,?,?,?,?,?,?::vector)
            ON CONFLICT (shipment_id) DO UPDATE SET
              month_key = EXCLUDED.month_key,
              embedding = EXCLUDED.embedding,
              indexed_at = now()
            """,
                shipmentId, m.get("shipperId"), m.get("originCity"), m.get("destinationCity"),
                m.get("slaType"), m.get("weightKg"), m.get("volumeM3"),
                m.get("requiresHazmat"), m.get("requiresColdChain"), m.get("monthKey"),
                vectorLiteral(embedding));
    }

    @Override
    public void upsertInventory(String warehouseId, float[] embedding, Map<String, Object> m) {
        jdbc.update("""
            INSERT INTO rag.inventory_embeddings
              (warehouse_id, warehouse_name, location,
               max_weight_kg, max_volume_m3, current_weight_kg, current_volume_m3, utilisation_pct, embedding)
            VALUES (?,?,?,?,?,?,?,?,?::vector)
            ON CONFLICT (warehouse_id) DO UPDATE SET
              current_weight_kg = EXCLUDED.current_weight_kg,
              current_volume_m3 = EXCLUDED.current_volume_m3,
              utilisation_pct = EXCLUDED.utilisation_pct,
              embedding = EXCLUDED.embedding,
              indexed_at = now()
            """,
                warehouseId, m.get("warehouseName"), m.get("location"),
                m.get("maxWeightKg"), m.get("maxVolumeM3"),
                m.get("currentWeightKg"), m.get("currentVolumeM3"), m.get("utilisationPct"),
                vectorLiteral(embedding));
    }

    // ── ANN queries ─────────────────────────────────────────────────────────

    @Override
    public List<Map<String, Object>> findSimilarRoutes(float[] queryEmbedding, int topK) {
        return jdbc.queryForList("""
            SELECT route_id, shipment_id, origin_city, destination_city, vehicle_type, sla_type,
                   distance_km, duration_minutes, fuel_cost_brl, toll_cost_brl, total_cost_brl,
                   1 - (embedding <=> ?::vector) AS similarity
            FROM rag.route_embeddings
            ORDER BY embedding <=> ?::vector
            LIMIT ?
            """, vectorLiteral(queryEmbedding), vectorLiteral(queryEmbedding), topK);
    }

    @Override
    public List<Map<String, Object>> findSimilarInvoices(float[] queryEmbedding, int topK) {
        return jdbc.queryForList("""
            SELECT invoice_id, shipment_id, shipper_id, carrier_id, origin_city, destination_city,
                   sla_type, base_amount_brl, penalty_days, penalty_amount_brl, total_amount_brl,
                   status, waiver_decision,
                   1 - (embedding <=> ?::vector) AS similarity
            FROM rag.invoice_embeddings
            WHERE status = 'PAID'
            ORDER BY embedding <=> ?::vector
            LIMIT ?
            """, vectorLiteral(queryEmbedding), vectorLiteral(queryEmbedding), topK);
    }

    @Override
    public List<Map<String, Object>> findSimilarShipments(float[] queryEmbedding, int topK) {
        return jdbc.queryForList("""
            SELECT shipment_id, shipper_id, origin_city, destination_city, sla_type,
                   weight_kg, volume_m3, month_key,
                   1 - (embedding <=> ?::vector) AS similarity
            FROM rag.shipment_embeddings
            ORDER BY embedding <=> ?::vector
            LIMIT ?
            """, vectorLiteral(queryEmbedding), vectorLiteral(queryEmbedding), topK);
    }

    @Override
    public List<Map<String, Object>> findAllInventory() {
        return jdbc.queryForList("""
            SELECT warehouse_id, warehouse_name, location,
                   max_weight_kg, max_volume_m3, current_weight_kg, current_volume_m3, utilisation_pct
            FROM rag.inventory_embeddings
            ORDER BY utilisation_pct DESC
            """);
    }

    private String vectorLiteral(float[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(v[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
