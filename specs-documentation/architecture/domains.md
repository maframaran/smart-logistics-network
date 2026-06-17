# Domain Model — Smart Logistics Network

Hexagonal Architecture applies to all services: the domain layer has zero dependencies on frameworks, Kafka, or persistence libraries. Infrastructure adapters implement domain-defined ports.

---

## Shipment Domain

### Aggregate Root: `Shipment`

```
ShipmentId           (value object — UUID)
ShipmentStatus       (DRAFT | CREATED | SCHEDULED | ASSIGNED | PICKED_UP | IN_TRANSIT | DELIVERED | CANCELLED | FAILED | RETURNED)
CargoSpec            (weight: kg, volume: m³, requiresHazmat: boolean, requiresColdChain: boolean)
Address              (street, city, country, coordinates: Coordinates)
DeliveryWindow       (promisedDeliveryDate: Instant, slaType: SlaType)
AssignmentRef        (vehicleId, driverId) — set when ASSIGNED
```

### Value Objects
- `ShipmentId` — UUID wrapper
- `Address` — immutable, includes `Coordinates(lat, lon)`
- `CargoSpec` — immutable cargo characteristics
- `DeliveryWindow` — contains `SlaType` (STANDARD | PRIORITY | EXPRESS)

### Domain Events
- `ShipmentCreated` → consumers: Routing, Pricing, Notification
- `ShipmentScheduled`
- `ShipmentAssigned` → consumers: Driver, Tracking, Analytics
- `ShipmentPickedUp` → consumers: Tracking, Notification
- `ShipmentInTransit`
- `ShipmentDelivered` → consumers: Billing, Analytics, Customer
- `ShipmentCancelled`
- `ShipmentFailed`

### Repository Port
```
ShipmentRepository:
  save(Shipment): void
  findById(ShipmentId): Optional<Shipment>
  findByStatus(ShipmentStatus): List<Shipment>
```

---

## Fleet Domain

### Aggregate Root: `Vehicle`

```
VehicleId            (value object — UUID)
VehicleType          (TRUCK | VAN | REFRIGERATED_TRUCK | HAZMAT_TRUCK)
Capacity             (maxWeightKg: int, maxVolumeM3: double)
VehicleStatus        (AVAILABLE | ASSIGNED | MAINTENANCE | OUT_OF_SERVICE)
Coordinates          (lat: double, lon: double) — current location
refrigerated:        boolean
hazmatCertified:     boolean
```

### Aggregate Root: `Driver`

```
DriverId             (value object — UUID)
LicenseType          (B | C | CE)
Certifications       (Set<Certification>) — includes HAZMAT
WorkingHoursLog      (dailyHoursToday: double, weeklyHours: double)
DriverStatus         (AVAILABLE | DRIVING | RESTING | SUSPENDED)
```

### Value Objects
- `VehicleId`, `DriverId` — UUID wrappers
- `Capacity` — immutable, enforces BR-001 / BR-002
- `Coordinates` — immutable lat/lon

### Domain Events
- `VehicleRegistered`
- `VehicleStatusChanged` → consumers: Shipment (for assignment availability)
- `DriverRegistered`
- `DriverStatusChanged`
- `DriverHoursExceeded` → consumers: Compliance, Notification

### Repository Ports
```
VehicleRepository:
  save(Vehicle): void
  findById(VehicleId): Optional<Vehicle>
  findAvailableByCapacity(CargoSpec): List<Vehicle>

DriverRepository:
  save(Driver): void
  findById(DriverId): Optional<Driver>
  findAvailableDrivers(): List<Driver>
```

---

## Warehouse Domain

### Aggregate Root: `Warehouse`

```
WarehouseId          (value object — UUID)
Location             (Address)
Capacity             (maxUnits: int, currentUnits: int)
OperatingHours       (open: LocalTime, close: LocalTime, daysOfWeek: Set<DayOfWeek>)
```

### Entity: `InventoryItem`

```
SKU                  (value object — String)
Quantity             int
WarehouseRef         WarehouseId
ExpirationDate       LocalDate (nullable)
BatchNumber          String (nullable)
```

### Value Objects
- `WarehouseId`, `SKU`
- `Capacity` — enforces BR-006 (`currentCapacity + incoming ≤ maxCapacity`)

### Domain Events
- `InventoryReceived`
- `InventoryDispatched`
- `WarehouseCapacityExceeded` → consumers: Operations, Notification

### Repository Ports
```
WarehouseRepository:
  save(Warehouse): void
  findById(WarehouseId): Optional<Warehouse>
  findWithAvailableCapacity(requiredUnits: int): List<Warehouse>

InventoryRepository:
  save(InventoryItem): void
  findBySku(SKU): List<InventoryItem>
```

---

## Routing Domain

### Aggregate Root: `Route`

```
RouteId              (value object — UUID)
ShipmentRef          ShipmentId
Segments             List<RouteSegment>
TotalDistanceKm      double
EstimatedDuration    Duration
FuelEstimateL        double
TollCostEur          double
CalculatedAt         Instant
```

### Value Objects
- `RouteSegment` — from/to Coordinates, distanceKm, durationMin
- `ETA` — Instant + confidence level
- `FuelEstimate` — litres, cost

### Domain Events
- `RouteCalculated` → consumers: Shipment (update ETA), Billing
- `RouteRecalculated` — triggered by vehicle breakdown or traffic

### Port: `RoutingEngine`
```
RoutingEngine (outbound port):
  calculate(origin: Coordinates, destination: Coordinates, vehicleType: VehicleType, deliveryWindow: DeliveryWindow): Route
```
Implemented by an adapter calling the external Maps/Routing API.

---

## Billing Domain

### Aggregate Root: `Invoice`

```
InvoiceId            (value object — UUID)
ShipmentRef          ShipmentId
ShipperRef           ShipperId
LineItems            List<InvoiceLineItem>
TotalAmountEur       BigDecimal
Status               (DRAFT | ISSUED | PAID | DISPUTED | CANCELLED)
IssuedAt             Instant
```

### Aggregate Root: `CarrierPayment`

```
PaymentId            (value object — UUID)
CarrierRef           CarrierId
ShipmentRef          ShipmentId
AmountEur            BigDecimal
Status               (PENDING | APPROVED | PAID | FAILED)
```

### Value Objects
- `InvoiceLineItem` — description, quantity, unitPriceEur, totalEur
- `SlaPenalty` — computed from `SlaType` and delivery delay (see `docs/overview.md` for rates)

### SLA Penalty Rates (from BR-004)
| SLA Type | Delivery Window | Penalty Rate |
|----------|----------------|-------------|
| STANDARD | 72h | 5% per hour late |
| PRIORITY | 24h | 15% per hour late |
| EXPRESS  | 6h  | 25% per hour late |

### Domain Events
- `InvoiceGenerated` → consumers: Notification, Analytics
- `InvoicePaid`
- `CarrierPaymentApproved`
- `SlaPenaltyApplied`

### Repository Ports
```
InvoiceRepository:
  save(Invoice): void
  findByShipmentId(ShipmentId): Optional<Invoice>

CarrierPaymentRepository:
  save(CarrierPayment): void
  findPendingByCarrier(CarrierId): List<CarrierPayment>
```
