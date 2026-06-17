package com.logistics.rag.domain.ports.out;

import java.util.List;
import java.util.Map;

public interface VectorStorePort {
    void upsertRoute(String routeId, float[] embedding, Map<String, Object> metadata);
    void upsertInvoice(String invoiceId, float[] embedding, Map<String, Object> metadata);
    void upsertShipment(String shipmentId, float[] embedding, Map<String, Object> metadata);
    void upsertInventory(String warehouseId, float[] embedding, Map<String, Object> metadata);

    List<Map<String, Object>> findSimilarRoutes(float[] queryEmbedding, int topK);
    List<Map<String, Object>> findSimilarInvoices(float[] queryEmbedding, int topK);
    List<Map<String, Object>> findSimilarShipments(float[] queryEmbedding, int topK);
    List<Map<String, Object>> findAllInventory();
}
