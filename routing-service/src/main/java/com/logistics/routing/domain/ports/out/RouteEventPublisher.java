package com.logistics.routing.domain.ports.out;

import com.logistics.common.domain.DomainEvent;

public interface RouteEventPublisher {
    void publish(DomainEvent event);
}
