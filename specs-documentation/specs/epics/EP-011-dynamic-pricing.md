# Epic: EP-011 — Dynamic Pricing

**Phase:** 4
**Domain:** Billing / AI

## Problem

Static pricing does not reflect real-time supply/demand, fuel prices, or route availability, reducing platform revenue and carrier retention during peak periods.

## Success Metrics

- Pricing adjusts in real time (< 5 second latency from signal to published price)
- Revenue per shipment increases 8% vs. static pricing (A/B tested)
- Carrier acceptance rate maintained above 85%

## Features

- Real-time demand signal ingestion (capacity utilization, fuel price feed)
- Dynamic price calculation engine
- Price surge notification to shippers before shipment confirmation
- Pricing audit trail per shipment

## Dependencies

- [EP-006](EP-006-billing.md)
- [EP-010](EP-010-demand-forecasting.md) — demand signals
