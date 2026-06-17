package com.logistics.shipment.domain.ports.in;

import com.logistics.shipment.domain.model.Address;
import com.logistics.shipment.domain.model.CargoSpec;
import com.logistics.shipment.domain.model.ShipmentId;
import com.logistics.shipment.domain.model.SlaType;

import java.time.LocalDate;

public interface CreateShipmentUseCase {

    ShipmentId create(Command command);

    record Command(
            String shipperId,
            Address origin,
            Address destination,
            CargoSpec cargoSpec,
            SlaType slaType,
            LocalDate requiredDeliveryDate
    ) {}
}
