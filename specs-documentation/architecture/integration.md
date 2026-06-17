# Integration Architecture — Smart Logistics Network

All cross-service communication is asynchronous via Apache Kafka. No service calls another service's REST API directly. Each service owns its topic(s) as producer and subscribes to foreign topics as a consumer through its inbound Kafka adapter.

---

## JPMS Module Graph

Each service module `requires` only `com.logistics.common` at the domain boundary. Inter-service communication is exclusively via Kafka — no service directly imports types from another service's module. This enforces bounded-context isolation at compile time.

```
com.logistics.common
  exports com.logistics.common.domain   // AggregateRoot, DomainEvent

com.logistics.shipment
  requires com.logistics.common
  requires spring.context, spring.tx, spring.web, spring.webmvc
  requires spring.boot, spring.boot.autoconfigure
  requires spring.data.jpa, spring.kafka
  requires jakarta.persistence, jakarta.annotation

com.logistics.fleet
  requires com.logistics.common
  requires spring.context, spring.tx, spring.web, spring.webmvc
  requires spring.boot, spring.boot.autoconfigure
  requires spring.data.jpa, spring.kafka
  requires jakarta.persistence, jakarta.annotation

com.logistics.driver
  requires com.logistics.common
  requires spring.context, spring.tx, spring.web, spring.webmvc
  requires spring.boot, spring.boot.autoconfigure
  requires spring.data.jpa, spring.kafka
  requires jakarta.persistence, jakarta.annotation

com.logistics.routing
  requires com.logistics.common
  requires spring.context, spring.tx, spring.web, spring.webmvc
  requires spring.boot, spring.boot.autoconfigure
  requires spring.data.jpa, spring.kafka
  requires jakarta.persistence, jakarta.annotation

com.logistics.warehouse
  requires com.logistics.common
  requires spring.context, spring.tx, spring.web, spring.webmvc
  requires spring.boot, spring.boot.autoconfigure
  requires spring.data.jpa, spring.kafka
  requires jakarta.persistence, jakarta.annotation

com.logistics.billing
  requires com.logistics.common
  requires spring.context, spring.tx, spring.web, spring.webmvc
  requires spring.boot, spring.boot.autoconfigure
  requires spring.data.jpa, spring.kafka
  requires jakarta.persistence, jakarta.annotation

com.logistics.notification
  requires com.logistics.common
  requires spring.context, spring.tx, spring.web, spring.webmvc
  requires spring.boot, spring.boot.autoconfigure
  requires spring.data.jpa, spring.kafka
  requires spring.context.support    // JavaMailSender
  requires spring.messaging          // @Payload, @Header in Kafka listeners
  requires jakarta.persistence, jakarta.annotation
  requires org.slf4j
```

**Key JPMS rules enforced:**
- Domain classes (`domain/model/`, `domain/events/`, `domain/ports/`) have zero `import org.springframework.*` or `import jakarta.persistence.*`
- Services never `requires` each other — all cross-service coupling goes through Kafka payloads (JSON maps)
- `spring.messaging` is required (not just `spring.kafka`) when using `@Payload`/`@Header` in Kafka listener methods, because Kafka's client jar is an unnamed module and `ConsumerRecord<>` cannot be used as a parameter type in a JPMS module class
- Non-obvious `Automatic-Module-Name` values: `spring-webmvc` → `spring.webmvc`; `spring-context-support` → `spring.context.support` (see [ADR-004](../adrs/ADR-004-jpms.md))

---

## Kafka Topic Catalogue

### `shipment.created`
| Field | Value |
|-------|-------|
| Producer | `com.logistics.shipment` |
| Consumers | `com.logistics.routing`, `com.logistics.billing`, `com.logistics.notification` |
| Key | `shipmentId` (string) |
| Partitions | 12 |
| Retention | 7 days |
| Payload | `ShipmentCreatedEvent { shipmentId, origin, destination, cargoSpec, slaType, createdAt }` |

---

### `shipment.assigned`
| Field | Value |
|-------|-------|
| Producer | `com.logistics.shipment` |
| Consumers | `com.logistics.driver`, `com.logistics.notification` |
| Key | `shipmentId` |
| Partitions | 12 |
| Retention | 7 days |
| Payload | `ShipmentAssignedEvent { shipmentId, vehicleId, driverId, routeId, etaAt }` |

---

### `shipment.picked-up`
| Field | Value |
|-------|-------|
| Producer | `com.logistics.shipment` |
| Consumers | `com.logistics.notification` |
| Key | `shipmentId` |
| Partitions | 12 |
| Retention | 7 days |
| Payload | `ShipmentPickedUpEvent { shipmentId, driverId, pickedUpAt, currentLocation }` |

---

### `shipment.delivered`
| Field | Value |
|-------|-------|
| Producer | `com.logistics.shipment` |
| Consumers | `com.logistics.billing`, `com.logistics.notification` |
| Key | `shipmentId` |
| Partitions | 12 |
| Retention | 30 days |
| Payload | `ShipmentDeliveredEvent { shipmentId, deliveredAt, signedBy, promisedDeliveryDate }` |

---

### `fleet.vehicle-status-changed`
| Field | Value |
|-------|-------|
| Producer | `com.logistics.fleet` |
| Consumers | `com.logistics.shipment` (availability for assignment) |
| Key | `vehicleId` |
| Partitions | 6 |
| Retention | 3 days |
| Payload | `VehicleStatusChangedEvent { vehicleId, previousStatus, newStatus, changedAt }` |

---

### `fleet.driver-status-changed`
| Field | Value |
|-------|-------|
| Producer | `com.logistics.driver` |
| Consumers | `com.logistics.shipment` (availability for assignment) |
| Key | `driverId` |
| Partitions | 6 |
| Retention | 3 days |
| Payload | `DriverStatusChangedEvent { driverId, previousStatus, newStatus, changedAt }` |

---

### `warehouse.capacity-updated`
| Field | Value |
|-------|-------|
| Producer | `com.logistics.warehouse` |
| Consumers | `com.logistics.routing` (route feasibility with warehouse stops), `com.logistics.notification` |
| Key | `warehouseId` |
| Partitions | 4 |
| Retention | 3 days |
| Payload | `WarehouseCapacityUpdatedEvent { warehouseId, currentUnits, maxUnits, updatedAt }` |

---

### `routing.route-calculated`
| Field | Value |
|-------|-------|
| Producer | `com.logistics.routing` |
| Consumers | `com.logistics.shipment` (update ETA), `com.logistics.billing` (fuel/toll costs) |
| Key | `shipmentId` |
| Partitions | 12 |
| Retention | 7 days |
| Payload | `RouteCalculatedEvent { routeId, shipmentId, totalDistanceKm, estimatedDurationMin, fuelEstimateL, tollCostEur, etaAt }` |

---

### `billing.invoice-generated`
| Field | Value |
|-------|-------|
| Producer | `com.logistics.billing` |
| Consumers | `com.logistics.notification` |
| Key | `invoiceId` |
| Partitions | 4 |
| Retention | 90 days |
| Payload | `InvoiceGeneratedEvent { invoiceId, shipmentId, shipperId, totalAmountEur, issuedAt }` |

---

## Event Schema Conventions

- All event payloads are JSON (Avro schema registry planned for Phase 3)
- All timestamps are ISO-8601 UTC (`Instant`)
- All IDs are UUID strings
- Events are immutable: once published, never modified
- Each event includes `eventId` (UUID) and `eventVersion` (int) for idempotency

---

## Docker Compose Services

```yaml
services:
  kafka:          # Apache Kafka (KRaft mode, no Zookeeper)
  shipment-svc:   # com.logistics.shipment module
  fleet-svc:      # com.logistics.fleet module
  driver-svc:     # com.logistics.driver module
  routing-svc:    # com.logistics.routing module
  warehouse-svc:  # com.logistics.warehouse module
  billing-svc:    # com.logistics.billing module
  notification-svc:
  postgres:       # shared DB in dev; each service owns its own schema
```

Each service container exposes a REST port and connects to Kafka and PostgreSQL.
