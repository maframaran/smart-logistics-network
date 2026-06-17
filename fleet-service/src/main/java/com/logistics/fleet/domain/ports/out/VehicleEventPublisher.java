package com.logistics.fleet.domain.ports.out;

import com.logistics.common.domain.DomainEvent;

public interface VehicleEventPublisher {

    void publish(DomainEvent event);
}
