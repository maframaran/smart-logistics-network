# Logistics UI Service

## Purpose

The customer-facing web portal for the Smart Logistics Network. Provides Shippers and Carriers with authenticated, role-scoped access to shipment tracking, fleet management, warehouse capacity, and billing — without calling backend APIs directly. Acts as a Backend-for-Frontend (BFF) for all 7 microservices.

## Owned Domain

UI / Presentation layer — owns no domain logic. All business rules remain in the backend services.

## Tech Stack

| Component | Choice |
|-----------|--------|
| Runtime | Node.js 22 (LTS) |
| Framework | Next.js 15 (App Router) |
| Language | TypeScript 5 |
| Styling | Tailwind CSS 3 |
| Components | shadcn/ui (owned, not a dependency) |
| Data fetching | TanStack Query v5 |
| HTTP client | `ky` |
| Auth | Auth.js v5 (next-auth) |
| Charts | Recharts |
| Port | 3000 |

## SLA / Availability

| Metric | Target |
|--------|--------|
| Uptime | 99.5% |
| Page load (P95) | < 2 seconds |
| Time to interactive (TTI P95) | < 3 seconds |

## Authentication

- Session strategy: JWT in httpOnly Secure cookie
- Session duration: 8 hours
- Roles: `SHIPPER` \| `CARRIER`
- Middleware: all routes except `/login` require a valid session
- Phase 5: OAuth 2.0 / SAML providers added via Auth.js configuration

## Environment Variables

| Variable | Example | Description |
|----------|---------|-------------|
| `SHIPMENT_SERVICE_URL` | `http://shipment-service:8081` | BFF proxy target |
| `FLEET_SERVICE_URL` | `http://fleet-service:8082` | BFF proxy target |
| `DRIVER_SERVICE_URL` | `http://driver-service:8083` | BFF proxy target |
| `ROUTING_SERVICE_URL` | `http://routing-service:8084` | BFF proxy target |
| `WAREHOUSE_SERVICE_URL` | `http://warehouse-service:8085` | BFF proxy target |
| `BILLING_SERVICE_URL` | `http://billing-service:8086` | BFF proxy target |
| `NOTIFICATION_SERVICE_URL` | `http://notification-service:8087` | BFF proxy target |
| `NEXTAUTH_SECRET` | `<random 32-byte string>` | Session signing key |
| `NEXTAUTH_URL` | `http://localhost:3000` | Canonical URL for redirects |

## Pages

| Route | Role | Description |
|-------|------|-------------|
| `/login` | All | Login page |
| `/dashboard` | All | Aggregate stats overview |
| `/shipments` | SHIPPER | Shipment list with status filter |
| `/shipments/[id]` | SHIPPER | Shipment detail + timeline |
| `/fleet` | CARRIER | Fleet board — vehicles + drivers |
| `/fleet/vehicles/[id]` | CARRIER | Vehicle detail |
| `/fleet/drivers/[id]` | CARRIER | Driver detail + hours bar |
| `/warehouse` | CARRIER | Warehouse capacity list |
| `/warehouse/[id]` | CARRIER | Warehouse detail + inventory table |
| `/billing` | SHIPPER | Invoice list |
| `/billing/[id]` | SHIPPER | Invoice detail + line items |

## BFF Proxy Table

| Browser request prefix | Forwarded to | Service |
|------------------------|-------------|---------|
| `/api/shipments/**` | `SHIPMENT_SERVICE_URL/api/v1/shipments/**` | shipment-service |
| `/api/vehicles/**` | `FLEET_SERVICE_URL/api/v1/vehicles/**` | fleet-service |
| `/api/drivers/**` | `DRIVER_SERVICE_URL/api/v1/drivers/**` | driver-service |
| `/api/routes/**` | `ROUTING_SERVICE_URL/api/v1/routes/**` | routing-service |
| `/api/warehouses/**` | `WAREHOUSE_SERVICE_URL/api/v1/warehouses/**` | warehouse-service |
| `/api/invoices/**` | `BILLING_SERVICE_URL/api/v1/invoices/**` | billing-service |
| `/api/notifications/**` | `NOTIFICATION_SERVICE_URL/api/v1/notifications/**` | notification-service |

## Kafka

None — logistics-ui communicates exclusively via the BFF REST proxy. No Kafka connection.

## Dependencies (runtime)

All 7 microservices must be healthy for full functionality. Graceful degradation: if a service is unavailable, the affected page section shows an error state; other sections remain functional.
