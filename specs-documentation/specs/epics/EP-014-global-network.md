# Epic: EP-014 — Global Logistics Network

**Phase:** 5
**Domain:** Routing / Shipment / Platform

## Problem

The platform only supports domestic shipments. International shipments require customs, border crossing rules, multi-currency billing, and multi-region infrastructure.

## Success Metrics

- International shipment creation supported in 20+ countries at launch
- Customs documentation generated automatically for 95% of cross-border shipments
- Platform latency SLAs maintained across regions (< 200ms P99)

## Features

- Multi-country address and routing support
- Customs documentation generation
- Multi-currency billing
- Multi-region deployment (data residency compliance)
- Cross-border regulatory rule engine

## Dependencies

- [EP-004](EP-004-route-optimization.md) — international routing
- [EP-006](EP-006-billing.md) — multi-currency
- [EP-013](EP-013-multi-tenant-saas.md) — regional tenants
