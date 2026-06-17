package com.logistics.shipment.domain.ports.out;

import com.logistics.common.domain.DomainEvent;

public interface ShipmentEventPublisher {

    void publish(DomainEvent event);
}
