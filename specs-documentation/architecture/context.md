# System Context — Smart Logistics Network

C4 Level 1: System Context

---

## Platform

**Smart Logistics Network** — orchestrates the movement of goods between shippers, warehouses, carriers, and customers. Implemented as a set of JPMS-modularized microservices communicating via Apache Kafka and REST.

---

## Actors

| Actor | Interaction |
|-------|-------------|
| **Shipper** | Creates shipment requests, monitors delivery progress, reviews invoices, manages SLAs |
| **Carrier** | Accepts shipment assignments, manages fleet availability, delivers cargo |
| **Driver** | Receives pickup/delivery instructions, updates shipment status, reports incidents |
| **Warehouse Operator** | Receives inbound inventory, prepares outbound shipments, manages storage |
| **Platform Administrator** | Configures business rules, pricing policies, user management, compliance settings |

---

## Internal Services

```
                        Browser (Shipper / Carrier)
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│              Smart Logistics Network                         │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           logistics-ui  :3000  (Next.js 15)         │   │
│  │   BFF — proxies REST to backend services            │   │
│  │   Auth.js sessions — scoped by SHIPPER / CARRIER    │   │
│  └──────────────────────────┬──────────────────────────┘   │
│                             │ REST (internal only)          │
│  ┌──────────────┐   ┌───────▼──────┐   ┌────────────┐     │
│  │  Shipment    │   │    Fleet     │   │   Driver   │     │
│  │  Service     │   │   Service    │   │  Service   │     │
│  └──────┬───────┘   └──────┬───────┘   └─────┬──────┘     │
│         │                  │                  │            │
│  ┌──────▼───────────────────▼──────────────────▼──────┐    │
│  │                  Kafka Event Bus                    │    │
│  └──────┬──────────────────┬──────────────────┬───────┘    │
│         │                  │                  │            │
│  ┌──────▼───────┐   ┌──────▼───────┐   ┌─────▼──────┐     │
│  │   Routing    │   │  Warehouse   │   │  Billing   │     │
│  │   Service    │   │   Service    │   │  Service   │     │
│  └──────────────┘   └──────────────┘   └────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

**logistics-ui** acts as a BFF (see [ADR-020](../adrs/ADR-020-bff-pattern.md)): the browser only communicates with `logistics-ui:3000`; all microservice ports are internal to the Docker network. Authentication is enforced at the UI layer (see [ADR-023](../adrs/ADR-023-authjs.md)).

---

## External Systems

| System | Purpose | Interaction |
|--------|---------|-------------|
| **Maps / Routing API** | Geocoding, road network, traffic data | Routing Service calls REST API to calculate routes and ETAs |
| **Payment Gateway** | Process carrier payments and customer invoices | Billing Service initiates payment transactions |
| **Notification Service (SMS/Email)** | Delivery status updates, incident alerts, SLA breach notifications | Notification adapter subscribes to domain events |
| **Customs / Regulatory API** | Border crossing requirements, hazmat regulations, food transport rules | Routing and Shipment services validate compliance |
| **Fuel Price Feed** | Real-time fuel cost data | Billing and Routing services consume for dynamic cost calculation |

---

## Communication Patterns

- **Synchronous (REST)**: Actor-facing APIs (shipment creation, status queries, fleet registration)
- **Asynchronous (Kafka)**: All cross-service domain events (see `integration.md`)
- **No direct service-to-service REST calls**: Services are decoupled exclusively through Kafka events
