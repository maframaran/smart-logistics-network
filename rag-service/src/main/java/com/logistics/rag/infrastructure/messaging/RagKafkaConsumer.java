package com.logistics.rag.infrastructure.messaging;

import com.logistics.rag.application.usecases.DemandForecastService;
import com.logistics.rag.application.usecases.InventoryAdvisorService;
import com.logistics.rag.application.usecases.PricingAdvisorService;
import com.logistics.rag.application.usecases.RouteSearchService;
import com.logistics.rag.application.usecases.WaiverAssistantService;
import com.logistics.rag.infrastructure.messaging.dto.InvoiceGeneratedPayload;
import com.logistics.rag.infrastructure.messaging.dto.RouteCalculatedPayload;
import com.logistics.rag.infrastructure.messaging.dto.ShipmentCreatedPayload;
import com.logistics.rag.infrastructure.messaging.dto.WarehouseCapacityUpdatedPayload;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

// Failures propagate to the container's DefaultErrorHandler (KafkaConsumerConfig, ADR-031),
// which retries with backoff and then routes to "<topic>.DLT" instead of being swallowed here.
@Component
public class RagKafkaConsumer {

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
    public void onRouteCalculated(@Payload RouteCalculatedPayload payload,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String routeId) {
        routeSearch.index(routeId, payload);
    }

    @KafkaListener(topics = "billing.invoice-generated", groupId = "rag-service")
    public void onInvoiceGenerated(@Payload InvoiceGeneratedPayload payload,
                                    @Header(KafkaHeaders.RECEIVED_KEY) String invoiceId) {
        waiver.index(invoiceId, payload);
        pricing.index(invoiceId, payload);
    }

    @KafkaListener(topics = "warehouse.capacity-updated", groupId = "rag-service")
    public void onCapacityUpdated(@Payload WarehouseCapacityUpdatedPayload payload,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String warehouseId) {
        inventory.index(warehouseId, payload);
    }

    @KafkaListener(topics = "shipment.created", groupId = "rag-service")
    public void onShipmentCreated(@Payload ShipmentCreatedPayload payload,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        forecast.index(shipmentId, payload);
    }
}
