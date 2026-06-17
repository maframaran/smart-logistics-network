package com.logistics.warehouse.domain.ports.out;

import com.logistics.common.domain.DomainEvent;

public interface WarehouseEventPublisher {
    void publish(DomainEvent event);
}
