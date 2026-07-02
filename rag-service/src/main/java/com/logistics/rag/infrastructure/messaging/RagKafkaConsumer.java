package com.logistics.rag.infrastructure.messaging;

import com.logistics.rag.application.usecases.DemandForecastService;
import com.logistics.rag.application.usecases.InventoryAdvisorService;
import com.logistics.rag.application.usecases.PricingAdvisorService;
import com.logistics.rag.application.usecases.RouteSearchService;
import com.logistics.rag.application.usecases.WaiverAssistantService;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

// Consumes Avro GenericRecord (KafkaAvroDeserializer), maps to typed DTO records, delegates to use cases.
// Failures propagate to the container's DefaultErrorHandler (KafkaConsumerConfig, ADR-031),
// which retries with backoff and then routes to "<topic>.DLT" instead of being swallowed here.
@Component
public class RagKafkaConsumer {

    private final RouteSearchService routeSearch;
    private final WaiverAssistantService waiver;
    private final PricingAdvisorService pricing;
    private final InventoryAdvisorService inventory;
    private final DemandForecastService forecast;
    private final RagEventAvroMapper avroMapper;

    public RagKafkaConsumer(RouteSearchService routeSearch, WaiverAssistantService waiver,
                             PricingAdvisorService pricing, InventoryAdvisorService inventory,
                             DemandForecastService forecast, RagEventAvroMapper avroMapper) {
        this.routeSearch = routeSearch;
        this.waiver = waiver;
        this.pricing = pricing;
        this.inventory = inventory;
        this.forecast = forecast;
        this.avroMapper = avroMapper;
    }

    @KafkaListener(topics = "routing.route-calculated", groupId = "rag-service")
    public void onRouteCalculated(@Payload GenericRecord record,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String routeId) {
        routeSearch.index(routeId, avroMapper.toRouteCalculated(record));
    }

    @KafkaListener(topics = "billing.invoice-generated", groupId = "rag-service")
    public void onInvoiceGenerated(@Payload GenericRecord record,
                                    @Header(KafkaHeaders.RECEIVED_KEY) String invoiceId) {
        var payload = avroMapper.toInvoiceGenerated(record);
        waiver.index(invoiceId, payload);
        pricing.index(invoiceId, payload);
    }

    @KafkaListener(topics = "warehouse.capacity-updated", groupId = "rag-service")
    public void onCapacityUpdated(@Payload GenericRecord record,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String warehouseId) {
        inventory.index(warehouseId, avroMapper.toWarehouseCapacityUpdated(record));
    }

    @KafkaListener(topics = "shipment.created", groupId = "rag-service")
    public void onShipmentCreated(@Payload GenericRecord record,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String shipmentId) {
        forecast.index(shipmentId, avroMapper.toShipmentCreated(record));
    }
}
