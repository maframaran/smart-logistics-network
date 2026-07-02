package com.logistics.warehouse.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.warehouse.domain.events.InventoryDispatched;
import com.logistics.warehouse.domain.events.InventoryReceived;
import com.logistics.warehouse.domain.events.WarehouseCapacityUpdated;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

@Component
class WarehouseEventAvroMapper {

    SpecificRecord toAvro(DomainEvent event) {
        return switch (event) {
            case InventoryReceived e        -> toAvro(e);
            case InventoryDispatched e      -> toAvro(e);
            case WarehouseCapacityUpdated e -> toAvro(e);
            default -> throw new IllegalArgumentException("Unknown event: " + event.getClass().getSimpleName());
        };
    }

    private com.logistics.warehouse.avro.InventoryReceived toAvro(InventoryReceived e) {
        return com.logistics.warehouse.avro.InventoryReceived.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setSku(e.sku())
                .setQuantity(e.quantity())
                .setCurrentWeightKg(e.currentWeightKg())
                .setCurrentVolumeM3(e.currentVolumeM3())
                .setMaxWeightKg(e.maxWeightKg())
                .setMaxVolumeM3(e.maxVolumeM3())
                .build();
    }

    private com.logistics.warehouse.avro.InventoryDispatched toAvro(InventoryDispatched e) {
        return com.logistics.warehouse.avro.InventoryDispatched.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setSku(e.sku())
                .setQuantity(e.quantity())
                .setCurrentWeightKg(e.currentWeightKg())
                .setCurrentVolumeM3(e.currentVolumeM3())
                .setMaxWeightKg(e.maxWeightKg())
                .setMaxVolumeM3(e.maxVolumeM3())
                .build();
    }

    private com.logistics.warehouse.avro.WarehouseCapacityUpdated toAvro(WarehouseCapacityUpdated e) {
        return com.logistics.warehouse.avro.WarehouseCapacityUpdated.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setCurrentWeightKg(e.currentWeightKg())
                .setCurrentVolumeM3(e.currentVolumeM3())
                .setMaxWeightKg(e.maxWeightKg())
                .setMaxVolumeM3(e.maxVolumeM3())
                .setUtilisationPct(e.utilisationPct())
                .build();
    }
}
