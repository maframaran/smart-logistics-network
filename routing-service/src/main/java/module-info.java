module com.logistics.routing {
    exports com.logistics.routing.domain.model;
    exports com.logistics.routing.domain.events;
    exports com.logistics.routing.domain.ports.in;
    exports com.logistics.routing.domain.ports.out;

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
