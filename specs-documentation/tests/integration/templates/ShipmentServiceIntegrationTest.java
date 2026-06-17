package com.logistics.shipment.tests.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

// Integration test — spec: specs/features/F-001-create-shipment.md
// Tests the full shipment-service adapter stack with real PostgreSQL and Kafka.
// No mocks. If it passes here, the wiring is correct.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CreateShipmentIT {

    @LocalServerPort
    int port;

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("shipment_test")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    // AC-001 + AC-003: spec: specs/features/F-001-create-shipment.md § Acceptance Criteria
    @Test
    void create_shipment_returns_201_with_shipmentId_and_CREATED_status() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "origin": { "street": "Industriestrasse 10", "city": "Berlin", "country": "DE" },
                  "destination": { "street": "Bahnhofstrasse 5", "city": "Munich", "country": "DE" },
                  "cargo": { "weightKg": 800, "volumeM3": 2.5, "requiresHazmat": false, "requiresColdChain": false },
                  "slaType": "STANDARD",
                  "requiredDeliveryDate": "2099-01-01T00:00:00Z"
                }
                """)
        .when()
            .post("/api/v1/shipments")
        .then()
            .statusCode(201)
            .body("shipmentId", notNullValue())
            .body("status", equalTo("CREATED"));
    }

    // AC-002: ShipmentCreated event published to Kafka
    @Test
    void create_shipment_publishes_ShipmentCreated_event() throws Exception {
        var response = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "origin": { "street": "Weg 1", "city": "Hamburg", "country": "DE" },
                  "destination": { "street": "Platz 2", "city": "Frankfurt", "country": "DE" },
                  "cargo": { "weightKg": 100, "volumeM3": 1.0, "requiresHazmat": false, "requiresColdChain": false },
                  "slaType": "EXPRESS",
                  "requiredDeliveryDate": "2099-06-01T00:00:00Z"
                }
                """)
            .post("/api/v1/shipments");

        String shipmentId = response.jsonPath().getString("shipmentId");

        // Poll shipment.created Kafka topic — message must arrive within 5 seconds
        // KafkaTestHelper is a shared utility in the test module
        var record = KafkaTestHelper.pollUntilKey("shipment.created", shipmentId, 5_000);
        assertThat(record).isNotNull();
        assertThat(record.value()).contains("\"eventType\":\"ShipmentCreated\"");
        assertThat(record.value()).contains("\"shipmentId\":\"" + shipmentId + "\"");
        assertThat(record.value()).contains("\"slaType\":\"EXPRESS\"");
    }

    // EC-003: spec: specs/features/F-001-create-shipment.md § Edge Cases EC-003
    @Test
    void create_shipment_rejects_negative_weight() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "origin": { "street": "A", "city": "B", "country": "DE" },
                  "destination": { "street": "C", "city": "D", "country": "DE" },
                  "cargo": { "weightKg": -100, "volumeM3": 1.0, "requiresHazmat": false, "requiresColdChain": false },
                  "slaType": "STANDARD",
                  "requiredDeliveryDate": "2099-01-01T00:00:00Z"
                }
                """)
        .when()
            .post("/api/v1/shipments")
        .then()
            .statusCode(400)
            .body("errorCode", equalTo("INVALID_CARGO_SPEC"));
    }
}
