module com.logistics.notification {
    exports com.logistics.notification.domain.model;
    exports com.logistics.notification.domain.events;
    exports com.logistics.notification.domain.ports.in;
    exports com.logistics.notification.domain.ports.out;

    requires com.logistics.common;
    requires spring.context;
    requires spring.tx;
    requires spring.web;
    requires spring.webmvc;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.data.jpa;
    requires spring.kafka;
    requires spring.context.support;
    requires spring.messaging;
    requires jakarta.persistence;
    requires jakarta.annotation;
    requires org.slf4j;
}
