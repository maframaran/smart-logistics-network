module com.logistics.driver {
    exports com.logistics.driver.domain.model;
    exports com.logistics.driver.domain.events;
    exports com.logistics.driver.domain.ports.in;
    exports com.logistics.driver.domain.ports.out;

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
