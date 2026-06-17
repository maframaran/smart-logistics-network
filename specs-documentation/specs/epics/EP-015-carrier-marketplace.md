# Epic: EP-015 — Carrier Marketplace

**Phase:** 5
**Domain:** Shipment / Fleet / Billing

## Problem

Shippers are limited to pre-approved carriers. A marketplace model enables open bidding, increasing carrier competition and reducing transportation costs for shippers.

## Success Metrics

- 50+ carriers onboarded at launch
- Average bid-to-award time under 10 minutes
- 15% reduction in average transportation cost vs. pre-negotiated rates

## Features

- Carrier self-registration and profile management
- Shipment load board (shippers post, carriers bid)
- Bid management (submit, accept, counter-offer)
- Carrier rating and review system
- Automated contract generation on award

## Domain Events Produced

- `CarrierOnboarded`
- `BidSubmitted`
- `BidAwarded`
- `ContractGenerated`

## Dependencies

- [EP-001](EP-001-shipment-management.md)
- [EP-002](EP-002-fleet-management.md)
- [EP-006](EP-006-billing.md)
- [EP-013](EP-013-multi-tenant-saas.md)
