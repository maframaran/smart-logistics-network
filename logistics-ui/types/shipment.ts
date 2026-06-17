export type ShipmentStatus =
  | "DRAFT"
  | "CREATED"
  | "SCHEDULED"
  | "ASSIGNED"
  | "PICKED_UP"
  | "IN_TRANSIT"
  | "DELIVERED"
  | "CANCELLED"
  | "FAILED"
  | "RETURNED";

export type SlaType = "STANDARD" | "PRIORITY" | "EXPRESS";

export interface Address {
  street: string;
  city: string;
  country: string;
  lat: number;
  lon: number;
}

export interface CargoSpec {
  weightKg: number;
  volumeM3: number;
  requiresHazmat: boolean;
  requiresColdChain: boolean;
}

export interface StatusTransition {
  status: ShipmentStatus;
  occurredAt: string; // ISO-8601
}

export interface Shipment {
  shipmentId: string;
  shipperId: string;
  origin: Address;
  destination: Address;
  cargo: CargoSpec;
  slaType: SlaType;
  requiredDeliveryDate: string; // ISO-8601 date
  status: ShipmentStatus;
  vehicleId?: string;
  vehiclePlate?: string;
  driverId?: string;
  driverName?: string;
  routeId?: string;
  deliveredAt?: string;
  statusHistory: StatusTransition[];
  createdAt: string;
}

export interface ShipmentListItem {
  shipmentId: string;
  shipperId: string;
  originCity: string;
  destinationCity: string;
  slaType: SlaType;
  status: ShipmentStatus;
  requiredDeliveryDate: string;
  createdAt: string;
}

export interface CreateShipmentRequest {
  shipperId: string;
  origin: Address;
  destination: Address;
  cargo: CargoSpec;
  slaType: SlaType;
  requiredDeliveryDate: string;
}
