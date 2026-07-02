module com.logistics.billing {
    exports com.logistics.billing.domain.model;
    exports com.logistics.billing.domain.events;
    exports com.logistics.billing.domain.ports.in;
    exports com.logistics.billing.domain.ports.out;

    requires static lombok;
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
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires org.apache.avro;
    requires org.slf4j;
}
