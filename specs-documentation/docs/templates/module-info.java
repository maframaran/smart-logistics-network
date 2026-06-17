// JPMS module descriptor — spec: adrs/ADR-004-jpms.md
// This file lives at src/main/java/module-info.java in the domain module.
//
// Rules enforced here:
//  - Domain packages are exported; internal packages are NOT
//  - Infrastructure frameworks (Spring, JPA, Kafka) are NOT required here
//  - Those go in the infrastructure module's own module-info.java
//
// See architecture/integration.md § JPMS Module Graph for the full dependency graph.

module com.logistics.shipment {

    // Public API of this domain — other modules may read these types
    exports com.logistics.shipment.domain.model;
    exports com.logistics.shipment.domain.events;
    exports com.logistics.shipment.domain.ports.in;
    exports com.logistics.shipment.domain.ports.out;

    // Shared value objects and common utilities
    requires com.logistics.common;

    // No spring.*, no jakarta.persistence, no org.apache.kafka here.
    // Those are infrastructure concerns — they belong in:
    //   module com.logistics.shipment.infrastructure { requires spring.boot; ... }
}
