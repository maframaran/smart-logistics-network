module com.logistics.fleet {
    exports com.logistics.fleet.domain.model;
    exports com.logistics.fleet.domain.events;
    exports com.logistics.fleet.domain.ports.in;
    exports com.logistics.fleet.domain.ports.out;

    requires io.swagger.v3.oas.annotations;
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
