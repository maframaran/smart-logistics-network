# Kafka Topic Catalogue — Smart Logistics Network

All cross-service communication is asynchronous via Apache Kafka (KRaft mode). No service calls another service's REST API directly.

---

## Topic Index

| Topic | Producer | Consumers | Partitions | Retention |
|-------|----------|-----------|-----------|-----------|
| [`shipment.created`](topics/shipment.created.md) | shipment-service | routing-service, billing-service, notification-service | 12 | 7 days |
| [`shipment.assigned`](topics/shipment.assigned.md) | shipment-service | driver-service, notification-service | 12 | 7 days |
| [`shipment.picked-up`](topics/shipment.picked-up.md) | shipment-service | notification-service | 12 | 7 days |
| [`shipment.delivered`](topics/shipment.delivered.md) | shipment-service | billing-service, notification-service | 12 | 30 days |
| [`shipment.cancelled`](topics/shipment.cancelled.md) | shipment-service | notification-service | 6 | 7 days |
| [`fleet.vehicle-status-changed`](topics/fleet.vehicle-status-changed.md) | fleet-service | shipment-service | 6 | 3 days |
| [`fleet.driver-status-changed`](topics/fleet.driver-status-changed.md) | driver-service | shipment-service | 6 | 3 days |
| [`warehouse.capacity-updated`](topics/warehouse.capacity-updated.md) | warehouse-service | routing-service, notification-service | 4 | 3 days |
| [`routing.route-calculated`](topics/routing.route-calculated.md) | routing-service | shipment-service, billing-service | 12 | 7 days |
| [`billing.invoice-generated`](topics/billing.invoice-generated.md) | billing-service | notification-service | 4 | 90 days |

---

## Conventions

- **Message key:** always the primary entity ID (e.g. `shipmentId`) for partition locality
- **Payload format:** JSON (Phase 3 will migrate to Avro + Schema Registry)
- **All timestamps:** ISO-8601 UTC (`Instant`)
- **All IDs:** UUID strings
- **Idempotency:** every event includes `eventId` (UUID) and `eventVersion` (int)
- **Dead-letter topics:** `<topic-name>.dlq` provisioned for every topic
- **Consumer groups:** one consumer group ID per service per topic (e.g. `routing-service.shipment.created`)
