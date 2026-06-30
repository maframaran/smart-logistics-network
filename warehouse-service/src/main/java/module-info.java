module com.logistics.warehouse {
    exports com.logistics.warehouse.domain.model;
    exports com.logistics.warehouse.domain.events;
    exports com.logistics.warehouse.domain.ports.in;
    exports com.logistics.warehouse.domain.ports.out;

    requires com.logistics.common;
    requires spring.beans;
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
