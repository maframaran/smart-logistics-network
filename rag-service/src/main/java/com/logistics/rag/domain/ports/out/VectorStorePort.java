package com.logistics.rag.domain.ports.out;

import com.logistics.rag.domain.model.InventoryMetadata;
import com.logistics.rag.domain.model.InventoryRow;
import com.logistics.rag.domain.model.InvoiceMetadata;
import com.logistics.rag.domain.model.InvoiceSearchRow;
import com.logistics.rag.domain.model.RouteMetadata;
import com.logistics.rag.domain.model.RouteSearchRow;
import com.logistics.rag.domain.model.ShipmentMetadata;
import com.logistics.rag.domain.model.ShipmentSearchRow;

import java.util.List;

public interface VectorStorePort {
    void upsertRoute(String routeId, float[] embedding, RouteMetadata metadata);
    void upsertInvoice(String invoiceId, float[] embedding, InvoiceMetadata metadata);
    void upsertShipment(String shipmentId, float[] embedding, ShipmentMetadata metadata);
    void upsertInventory(String warehouseId, float[] embedding, InventoryMetadata metadata);

    List<RouteSearchRow> findSimilarRoutes(float[] queryEmbedding, int topK);
    List<InvoiceSearchRow> findSimilarInvoices(float[] queryEmbedding, int topK);
    List<ShipmentSearchRow> findSimilarShipments(float[] queryEmbedding, int topK);
    List<InventoryRow> findAllInventory();
}
