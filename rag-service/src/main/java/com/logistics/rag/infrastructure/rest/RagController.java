package com.logistics.rag.infrastructure.rest;

import com.logistics.rag.application.usecases.DemandForecastService;
import com.logistics.rag.application.usecases.InventoryAdvisorService;
import com.logistics.rag.application.usecases.PricingAdvisorService;
import com.logistics.rag.application.usecases.RouteSearchService;
import com.logistics.rag.application.usecases.WaiverAssistantService;
import com.logistics.rag.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Tag(name = "RAG Intelligence", description = "pgvector + Claude API: route similarity, waiver assistant, dynamic pricing, inventory advisor, demand forecast")
@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RouteSearchService routeSearch;
    private final WaiverAssistantService waiver;
    private final PricingAdvisorService pricing;
    private final InventoryAdvisorService inventory;
    private final DemandForecastService forecast;

    public RagController(RouteSearchService routeSearch, WaiverAssistantService waiver,
                          PricingAdvisorService pricing, InventoryAdvisorService inventory,
                          DemandForecastService forecast) {
        this.routeSearch = routeSearch;
        this.waiver = waiver;
        this.pricing = pricing;
        this.inventory = inventory;
        this.forecast = forecast;
    }

    // F-026 — Route Similarity
    @Operation(summary = "Find similar historical routes", description = "Vector similarity search over previously calculated routes (F-026).")
    @GetMapping("/routes/similar")
    public RouteSearchResult similarRoutes(
            @RequestParam String originCity,
            @RequestParam String destinationCity,
            @RequestParam(defaultValue = "TRUCK") String vehicleType,
            @RequestParam(defaultValue = "STANDARD") String slaType) {
        return routeSearch.search(originCity, destinationCity, vehicleType, slaType);
    }

    // F-027 — Waiver Assistant
    @Operation(summary = "Recommend an SLA penalty waiver decision", description = "WAIVE/UPHOLD/ESCALATE based on historical precedents; confidence < 0.6 always escalates (F-027).")
    @PostMapping("/invoices/{invoiceId}/waiver")
    public WaiverResult waiverRecommendation(@PathVariable String invoiceId,
                                              @RequestBody WaiverRecommendationRequest body) {
        String reason = Objects.requireNonNullElse(body.reason(), "");
        return waiver.recommend(invoiceId, reason);
    }

    // F-028 — Dynamic Pricing
    @Operation(summary = "Recommend a dynamic price", description = "Suggested price capped at 1.5x the static rate, based on historical PAID invoices (F-028).")
    @PostMapping("/pricing/recommend")
    public PriceRecommendation pricingRecommendation(@RequestBody PricingRecommendationRequest body) {
        String originCity = Objects.requireNonNullElse(body.originCity(), "");
        String destinationCity = Objects.requireNonNullElse(body.destinationCity(), "");
        double weightKg = body.weightKg();
        String slaType = Objects.requireNonNullElse(body.slaType(), "STANDARD");
        int warehouseUtil = Objects.requireNonNullElse(body.warehouseUtilizationPct(), 50);
        return pricing.recommend(originCity, destinationCity, weightKg, slaType, warehouseUtil);
    }

    // F-029 — Inventory Advisor
    @Operation(summary = "Recommend inventory rebalancing", description = "SKU-level move suggestions when a warehouse exceeds 80% utilisation (F-029).")
    @GetMapping("/warehouses/{warehouseId}/rebalance")
    public InventoryRecommendation inventoryRebalance(@PathVariable String warehouseId) {
        return inventory.rebalance(warehouseId);
    }

    // F-030 — Demand Forecast
    @Operation(summary = "Forecast shipment demand", description = "Expected shipment count + confidence interval for a shipper/route/month, with a +20% bonus for matching calendar months (F-030).")
    @GetMapping("/forecast")
    public DemandForecast demandForecast(
            @RequestParam String shipperId,
            @RequestParam(defaultValue = "") String originCity,
            @RequestParam(defaultValue = "") String destinationCity,
            @RequestParam(defaultValue = "") String targetMonth) {
        return forecast.forecast(shipperId, originCity, destinationCity, targetMonth);
    }

}
