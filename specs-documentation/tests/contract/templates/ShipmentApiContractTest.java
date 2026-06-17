package com.logistics.shipment.tests.contract;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

// Contract test — validates HTTP responses match services/shipment-service/service.md.
// Each test is annotated with the spec section it validates.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShipmentApiContractTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    // Contract: services/shipment-service/service.md § POST /api/v1/shipments § Response 201
    // Validates: required fields shipmentId, status, createdAt are present and typed correctly
    @Test
    void post_shipments_response_has_required_fields() {
        given()
            .contentType(ContentType.JSON)
            .body(validCreateRequest())
        .when()
            .post("/api/v1/shipments")
        .then()
            .statusCode(201)
            .body("shipmentId", matchesPattern(
                    "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) // UUID format
            .body("status", equalTo("CREATED"))                // spec: status is exactly "CREATED"
            .body("createdAt", matchesPattern(".*T.*Z"));      // ISO-8601 UTC
    }

    // Contract: services/shipment-service/service.md § POST /api/v1/shipments § Error 400
    // Validates: error response includes errorCode field
    @Test
    void post_shipments_invalid_payload_returns_400_with_errorCode() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")               // empty body — missing required fields
        .when()
            .post("/api/v1/shipments")
        .then()
            .statusCode(400)
            .body("errorCode", notNullValue());
    }

    // Contract: services/shipment-service/service.md § GET /api/v1/shipments/{shipmentId}
    // Validates: all required fields present including nested objects
    @Test
    void get_shipment_by_id_returns_full_contract_shape() {
        // First create a shipment to get a valid ID
        String shipmentId = given()
            .contentType(ContentType.JSON)
            .body(validCreateRequest())
            .post("/api/v1/shipments")
            .jsonPath().getString("shipmentId");

        given()
        .when()
            .get("/api/v1/shipments/" + shipmentId)
        .then()
            .statusCode(200)
            .body("shipmentId", equalTo(shipmentId))
            .body("status", notNullValue())
            .body("origin.street", notNullValue())     // nested origin object required
            .body("origin.city", notNullValue())
            .body("origin.country", notNullValue())
            .body("destination.street", notNullValue())
            .body("cargo.weightKg", notNullValue())
            .body("cargo.volumeM3", notNullValue())
            .body("cargo.requiresHazmat", notNullValue())
            .body("cargo.requiresColdChain", notNullValue())
            .body("slaType", oneOf("STANDARD", "PRIORITY", "EXPRESS"))
            .body("createdAt", notNullValue())
            .body("updatedAt", notNullValue());
    }

    // Contract: services/shipment-service/service.md § GET /api/v1/shipments/{shipmentId} § 404
    @Test
    void get_shipment_by_unknown_id_returns_404() {
        given()
        .when()
            .get("/api/v1/shipments/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404);
    }

    private String validCreateRequest() {
        return """
            {
              "origin": { "street": "Street 1", "city": "Berlin", "country": "DE" },
              "destination": { "street": "Street 2", "city": "Munich", "country": "DE" },
              "cargo": { "weightKg": 500, "volumeM3": 2.0, "requiresHazmat": false, "requiresColdChain": false },
              "slaType": "STANDARD",
              "requiredDeliveryDate": "2099-01-01T00:00:00Z"
            }
            """;
    }
}
