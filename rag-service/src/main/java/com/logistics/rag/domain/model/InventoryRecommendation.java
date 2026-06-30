package com.logistics.rag.domain.model;

import java.util.List;

public record InventoryRecommendation(
        List<RebalanceAction> recommendations,
        String reason
) {
    public InventoryRecommendation {
        recommendations = List.copyOf(recommendations);
    }

    public record RebalanceAction(
            String sku,
            int suggestedQtyToMove,
            String targetWarehouseId,
            double fillPctAfter,
            String reasoning
    ) {}
}
