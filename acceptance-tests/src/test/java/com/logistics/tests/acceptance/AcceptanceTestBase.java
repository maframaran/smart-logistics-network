package com.logistics.tests.acceptance;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// Base class for all acceptance tests.
// Tests run against a live stack (docker compose up) — no Spring context is started here.
// Override service URLs via environment variables for CI or remote environments.
public abstract class AcceptanceTestBase {

    protected static final String SHIPMENT_URL  = env("SHIPMENT_SERVICE_URL",  "http://localhost:8081");
    protected static final String FLEET_URL     = env("FLEET_SERVICE_URL",     "http://localhost:8082");
    protected static final String DRIVER_URL    = env("DRIVER_SERVICE_URL",    "http://localhost:8083");
    protected static final String ROUTING_URL   = env("ROUTING_SERVICE_URL",   "http://localhost:8084");
    protected static final String WAREHOUSE_URL = env("WAREHOUSE_SERVICE_URL", "http://localhost:8085");
    protected static final String BILLING_URL   = env("BILLING_SERVICE_URL",   "http://localhost:8086");
    protected static final String RAG_URL       = env("RAG_SERVICE_URL",       "http://localhost:8088");
    protected static final String KAFKA_SERVERS = env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
    protected static final String UI_URL        = env("UI_BASE_URL",           "http://localhost:3000");

    protected static String env(String key, String fallback) {
        return System.getenv().getOrDefault(key, fallback);
    }

    // Fixed-delay wait for rag-service's async Kafka-driven indexing to catch up before the
    // next step queries it. Uses CompletableFuture's delayed executor rather than Thread.sleep
    // so the wait isn't tied to the calling thread via a blocking sleep call.
    protected static void awaitMillis(long millis) {
        CompletableFuture.runAsync(() -> {}, CompletableFuture.delayedExecutor(millis, TimeUnit.MILLISECONDS)).join();
    }

    // Convenience accessors so step definitions read as named service calls.
    protected static String shipmentUrl()  { return SHIPMENT_URL; }
    protected static String fleetUrl()     { return FLEET_URL; }
    protected static String driverUrl()    { return DRIVER_URL; }
    protected static String routingUrl()   { return ROUTING_URL; }
    protected static String warehouseUrl() { return WAREHOUSE_URL; }
    protected static String billingUrl()   { return BILLING_URL; }
    protected static String ragUrl()       { return RAG_URL; }
    protected static String uiUrl()        { return UI_URL; }
}
