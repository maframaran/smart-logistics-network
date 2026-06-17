module com.logistics.shipment {
    exports com.logistics.shipment.domain.model;
    exports com.logistics.shipment.domain.events;
    exports com.logistics.shipment.domain.ports.in;
    exports com.logistics.shipment.domain.ports.out;

    requires com.logistics.common;
    requires spring.context;
    requires spring.tx;
    requires spring.web;
    requires spring.webmvc;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.data.jpa;
    requires spring.kafka;
    requires jakarta.persistence;
    requires jakarta.annotation;
}
