# Epic: EP-010 — Demand Forecasting

**Phase:** 4
**Domain:** AI / Analytics

## Problem

Operations teams cannot predict shipment volume peaks, fleet demand, or warehouse utilization, leading to under/over provisioning.

## Success Metrics

- Shipment volume forecast accuracy within 10% (7-day horizon)
- Fleet demand predictions enable 90% vehicle pre-positioning accuracy
- Warehouse utilization predictions prevent overflow events

## Features

- Shipment volume prediction model
- Fleet demand prediction model
- Warehouse utilization prediction model
- Forecast API consumed by platform admin dashboard

## Data Sources

- Historical shipment events from Kafka (replayed from topic log)
- Seasonal patterns, external demand signals

## Dependencies

- [EP-007](EP-007-event-driven-architecture.md) — historical event log as training data
