package com.logistics.rag.infrastructure.rest;

import com.logistics.rag.application.usecases.DemandForecastService;
import com.logistics.rag.application.usecases.InventoryAdvisorService;
import com.logistics.rag.application.usecases.PricingAdvisorService;
import com.logistics.rag.application.usecases.RouteSearchService;
import com.logistics.rag.application.usecases.WaiverAssistantService;
import com.logistics.rag.domain.model.*;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

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
    @GetMapping("/routes/similar")
    public RouteSearchResult similarRoutes(
            @RequestParam String originCity,
            @RequestParam String destinationCity,
            @RequestParam(defaultValue = "TRUCK") String vehicleType,
            @RequestParam(defaultValue = "STANDARD") String slaType) {
        return routeSearch.search(originCity, destinationCity, vehicleType, slaType);
    }

    // F-027 — Waiver Assistant
    @PostMapping("/invoices/{invoiceId}/waiver")
    public WaiverResult waiverRecommendation(@PathVariable String invoiceId,
                                              @RequestBody WaiverRecommendationRequest body) {
        String reason = Objects.requireNonNullElse(body.reason(), "");
        return waiver.recommend(invoiceId, reason);
    }

    // F-028 — Dynamic Pricing
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
    @GetMapping("/warehouses/{warehouseId}/rebalance")
    public InventoryRecommendation inventoryRebalance(@PathVariable String warehouseId) {
        return inventory.rebalance(warehouseId);
    }

    // F-030 — Demand Forecast
    @GetMapping("/forecast")
    public DemandForecast demandForecast(
            @RequestParam String shipperId,
            @RequestParam(defaultValue = "") String originCity,
            @RequestParam(defaultValue = "") String destinationCity,
            @RequestParam(defaultValue = "") String targetMonth) {
        return forecast.forecast(shipperId, originCity, destinationCity, targetMonth);
    }

}
