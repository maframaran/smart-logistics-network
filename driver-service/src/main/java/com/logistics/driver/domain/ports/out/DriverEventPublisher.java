package com.logistics.driver.domain.ports.out;

import com.logistics.common.domain.DomainEvent;

public interface DriverEventPublisher {

    void publish(DomainEvent event);
}
