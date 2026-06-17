package com.logistics.rag.infrastructure.messaging;

import com.logistics.rag.application.usecases.DemandForecastService;
import com.logistics.rag.application.usecases.InventoryAdvisorService;
import com.logistics.rag.application.usecases.PricingAdvisorService;
import com.logistics.rag.application.usecases.RouteSearchService;
import com.logistics.rag.application.usecases.WaiverAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RagKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(RagKafkaConsumer.class);

    private final RouteSearchService routeSearch;
    private final WaiverAssistantService waiver;
    private final PricingAdvisorService pricing;
    private final InventoryAdvisorService inventory;
    private final DemandForecastService forecast;

    public RagKafkaConsumer(RouteSearchService routeSearch, WaiverAssistantService waiver,
                             PricingAdvisorService pricing, InventoryAdvisorService inventory,
                             DemandForecastService forecast) {
        this.routeSearch = routeSearch;
        this.waiver = waiver;
        this.pricing = pricing;
        this.inventory = inventory;
        this.forecast = forecast;
    }

    @KafkaListener(topics = "routing.route-calculated", groupId = "rag-service")
    public void onRouteCalculated(@Payload Map<String, Object> payload,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String routeId) {
        try {
            routeSearch.index(routeId, payload);
        } catch (Exception e) {
            log.error("Failed to index route {}: {}", routeId, e.getMessage());
        }
    }

    @KafkaListener(topics = "billing.invoice-generated", groupId = "rag-service")
    public void onInvoiceGenerated(@Payload Map<String, Object> payload,
                                    @Header(KafkaHeaders.RECEIVED_KEY) String invoiceId) {
        try {
            waiver.index(invoiceId, payload);
            pricing.index(invoiceId, payload);
        } catch (Exception e) {
            log.error("Failed to index invoice {}: {}", invoiceId, e.getMessage());
        }
    }

    @KafkaListener(topics = "warehouse.capacity-updated", groupId = "rag-service")
    public void onCapacityUpdated(@Payload Map<String, Object> payload,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String warehouseId) {
        try {
            inventory.index(warehouseId, payload);
        } catch (Exception e) {
            log.error("Failed to index warehouse {}: {}", warehouseId, e.getMessage());
        }
    }

    @KafkaListener(topics = "shipment.created", groupId = "rag-service")
    public void onShipmentCreated(@Payload Map<String, Object> payload,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        try {
            forecast.index(shipmentId, payload);
        } catch (Exception e) {
            log.error("Failed to index shipment {}: {}", shipmentId, e.getMessage());
        }
    }
}
