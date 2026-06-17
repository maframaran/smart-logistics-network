package com.logistics.tests.acceptance.runners;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

// Base class for all acceptance tests.
// Starts real PostgreSQL and Kafka containers — no mocks for infrastructure.
// Each test gets the same containers (reused via static fields for performance).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AcceptanceTestBase {

    @LocalServerPort
    protected int port;

    // PostgreSQL — matches docker-compose.yml image (see adrs/ADR-003-docker.md)
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("logistics_test")
                    .withUsername("test")
                    .withPassword("test");

    // Kafka KRaft mode — no ZooKeeper (see adrs/ADR-002-kafka.md)
    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }
}
