package com.logistics.billing.domain.ports.out;

import com.logistics.common.domain.DomainEvent;

public interface BillingEventPublisher {
    void publish(DomainEvent event);
}
