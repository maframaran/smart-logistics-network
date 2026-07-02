package com.logistics.rag.infrastructure.messaging;

import com.logistics.rag.infrastructure.messaging.dto.AddressPayload;
import com.logistics.rag.infrastructure.messaging.dto.CargoSpecPayload;
import com.logistics.rag.infrastructure.messaging.dto.InvoiceGeneratedPayload;
import com.logistics.rag.infrastructure.messaging.dto.MoneyPayload;
import com.logistics.rag.infrastructure.messaging.dto.RouteCalculatedPayload;
import com.logistics.rag.infrastructure.messaging.dto.ShipmentCreatedPayload;
import com.logistics.rag.infrastructure.messaging.dto.WarehouseCapacityUpdatedPayload;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// Maps Avro GenericRecord (KafkaAvroDeserializer) → typed DTO records (ADR-026, ADR-032).
@Component
class RagEventAvroMapper {

    RouteCalculatedPayload toRouteCalculated(GenericRecord r) {
        return new RouteCalculatedPayload(
                str(r, "shipmentId"),
                null,  // originCity: not in RouteCalculated event schema — pre-existing gap
                null,  // destinationCity: not in RouteCalculated event schema — pre-existing gap
                str(r, "vehicleType"),
                dbl(r, "totalDistanceKm"),
                lng(r, "totalDurationMinutes"),
                dbl(r, "fuelCostBrl"),
                dbl(r, "tollsCostBrl")
        );
    }

    InvoiceGeneratedPayload toInvoiceGenerated(GenericRecord r) {
        return new InvoiceGeneratedPayload(
                str(r, "shipmentId"),
                str(r, "shipperId"),
                str(r, "carrierId"),
                null,  // originCity: not in InvoiceGenerated event schema — pre-existing gap
                null,  // destinationCity: not in InvoiceGenerated event schema — pre-existing gap
                null,  // slaType: not in InvoiceGenerated event schema — pre-existing gap
                new MoneyPayload(decimal(r, "baseAmountBrl"), str(r, "baseCurrency")),
                lng(r, "penaltyDaysLate"),
                new MoneyPayload(decimal(r, "penaltyAmountBrl"), str(r, "penaltyCurrency")),
                new MoneyPayload(decimal(r, "totalAmountBrl"), str(r, "totalCurrency"))
        );
    }

    WarehouseCapacityUpdatedPayload toWarehouseCapacityUpdated(GenericRecord r) {
        return new WarehouseCapacityUpdatedPayload(
                null,  // warehouseName: not in WarehouseCapacityUpdated event schema — pre-existing gap
                null,  // location: not in WarehouseCapacityUpdated event schema — pre-existing gap
                dbl(r, "maxWeightKg"),
                dbl(r, "maxVolumeM3"),
                dbl(r, "currentWeightKg"),
                dbl(r, "currentVolumeM3"),
                dbl(r, "utilisationPct")
        );
    }

    ShipmentCreatedPayload toShipmentCreated(GenericRecord r) {
        GenericRecord originRecord = (GenericRecord) r.get("origin");
        GenericRecord destRecord   = (GenericRecord) r.get("destination");
        GenericRecord cargoRecord  = (GenericRecord) r.get("cargoSpec");
        return new ShipmentCreatedPayload(
                str(r, "shipperId"),
                str(r, "slaType"),
                originRecord  != null ? new AddressPayload(str(originRecord, "city"))  : null,
                destRecord    != null ? new AddressPayload(str(destRecord,   "city"))  : null,
                cargoRecord   != null ? new CargoSpecPayload(
                        dbl(cargoRecord, "weightKg"),
                        dbl(cargoRecord, "volumeM3"),
                        bool(cargoRecord, "requiresHazmat"),
                        bool(cargoRecord, "requiresColdChain")
                ) : null
        );
    }

    private static String str(GenericRecord r, String field) {
        Object v = r.get(field);
        return v == null ? null : v.toString();
    }

    private static double dbl(GenericRecord r, String field) {
        Object v = r.get(field);
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }

    private static long lng(GenericRecord r, String field) {
        Object v = r.get(field);
        return v instanceof Number n ? n.longValue() : 0L;
    }

    private static boolean bool(GenericRecord r, String field) {
        Object v = r.get(field);
        return Boolean.TRUE.equals(v);
    }

    private static BigDecimal decimal(GenericRecord r, String field) {
        Object v = r.get(field);
        if (v == null) return BigDecimal.ZERO;
        return new BigDecimal(v.toString());
    }
}
