package com.logistics.routing.infrastructure.messaging;

import com.logistics.common.domain.DomainEvent;
import com.logistics.routing.domain.events.RouteCalculated;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

@Component
class RoutingEventAvroMapper {

    SpecificRecord toAvro(DomainEvent event) {
        return switch (event) {
            case RouteCalculated e -> toAvro(e);
            default -> throw new IllegalArgumentException("Unknown event: " + event.getClass().getSimpleName());
        };
    }

    private com.logistics.routing.avro.RouteCalculated toAvro(RouteCalculated e) {
        return com.logistics.routing.avro.RouteCalculated.newBuilder()
                .setEventId(e.eventId().toString())
                .setOccurredAt(e.occurredAt().toEpochMilli())
                .setAggregateId(e.aggregateId())
                .setShipmentId(e.shipmentId())
                .setVehicleType(e.vehicleType())
                .setTotalDistanceKm(e.totalDistanceKm())
                .setTotalDurationMinutes(e.totalDurationMinutes())
                .setEstimatedArrival(e.estimatedArrival().toEpochMilli())
                .setFuelLitres(e.fuelLitres())
                .setFuelCostBrl(e.fuelCostBrl())
                .setTollsCostBrl(e.tollsCostBrl())
                .build();
    }
}
