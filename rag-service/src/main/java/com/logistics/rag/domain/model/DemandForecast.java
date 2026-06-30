package com.logistics.rag.domain.model;

import java.util.List;

public record DemandForecast(
        int expectedShipments,
        ConfidenceInterval confidenceInterval,
        List<ForecastComparable> comparables,
        boolean calendarBonus
) {
    public DemandForecast {
        comparables = List.copyOf(comparables);
    }

    public record ConfidenceInterval(int low, int high) {}
    public record ForecastComparable(String month, int actual) {}
}
