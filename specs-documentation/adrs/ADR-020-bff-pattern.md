# ADR-020 — Backend-for-Frontend (BFF) via Next.js API Routes

**Status:** Accepted

---

## Context

The browser cannot call 7 microservices directly because:
1. Each service runs on a different port (8081–8087) — CORS must be configured on every service
2. Auth tokens would need to be stored in `localStorage` or as accessible JS cookies — a security risk
3. Internal service URLs would be visible to the browser — port scanning exposure

---

## Decision

All browser requests go exclusively to Next.js on port 3000. Next.js API Routes (`/app/api/*/[...path]/route.ts`) act as a **Backend-for-Frontend proxy**:

```
Browser
  └─► Next.js :3000 /api/shipments/...
        └─► shipment-service :8081 /api/v1/shipments/...
```

Each catch-all route:
1. Validates the session (Auth.js `getServerSession`)
2. Forwards the request to the corresponding microservice URL (read from env var)
3. Passes through query params, request body, and method
4. Returns the microservice response to the browser

### Service URL mapping

| BFF route prefix | Env var | Microservice |
|-----------------|---------|--------------|
| `/api/shipments` | `SHIPMENT_SERVICE_URL` | `http://shipment-service:8081` |
| `/api/vehicles` | `FLEET_SERVICE_URL` | `http://fleet-service:8082` |
| `/api/drivers` | `DRIVER_SERVICE_URL` | `http://driver-service:8083` |
| `/api/routes` | `ROUTING_SERVICE_URL` | `http://routing-service:8084` |
| `/api/warehouses` | `WAREHOUSE_SERVICE_URL` | `http://warehouse-service:8085` |
| `/api/invoices` | `BILLING_SERVICE_URL` | `http://billing-service:8086` |
| `/api/notifications` | `NOTIFICATION_SERVICE_URL` | `http://notification-service:8087` |

---

## Consequences

- Microservices do not need CORS headers at all — they only receive requests from Next.js, which is a server-to-server call
- Auth cookies are httpOnly and `Secure` — never accessible from browser JavaScript
- Internal service ports are never reachable from outside the Docker network
- The BFF is a thin passthrough — no business logic lives here; it stays in the domain services
- Adding a new microservice requires one new catch-all route file — a 10-line addition
