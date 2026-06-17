import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  // Allow images from any logistics service
  images: { unoptimized: true },
  // Expose env vars to server-side BFF routes only (not to browser)
  serverRuntimeConfig: {
    shipmentServiceUrl: process.env.SHIPMENT_SERVICE_URL ?? "http://localhost:8081",
    fleetServiceUrl: process.env.FLEET_SERVICE_URL ?? "http://localhost:8082",
    driverServiceUrl: process.env.DRIVER_SERVICE_URL ?? "http://localhost:8083",
    routingServiceUrl: process.env.ROUTING_SERVICE_URL ?? "http://localhost:8084",
    warehouseServiceUrl: process.env.WAREHOUSE_SERVICE_URL ?? "http://localhost:8085",
    billingServiceUrl: process.env.BILLING_SERVICE_URL ?? "http://localhost:8086",
    notificationServiceUrl: process.env.NOTIFICATION_SERVICE_URL ?? "http://localhost:8087",
  },
};

export default nextConfig;
