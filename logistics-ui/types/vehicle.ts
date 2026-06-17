export type VehicleStatus = "AVAILABLE" | "ASSIGNED" | "MAINTENANCE" | "OUT_OF_SERVICE";

export type VehicleType =
  | "TRUCK"
  | "REFRIGERATED_TRUCK"
  | "HAZMAT_TRUCK"
  | "VAN"
  | "MOTORCYCLE";

export interface Vehicle {
  vehicleId: string;
  carrierId: string;
  licensePlate: string;
  type: VehicleType;
  weightCapacityKg: number;
  volumeCapacityM3: number;
  refrigerated: boolean;
  hazmatCertified: boolean;
  status: VehicleStatus;
  createdAt: string;
}

export interface VehicleListItem {
  vehicleId: string;
  licensePlate: string;
  type: VehicleType;
  weightCapacityKg: number;
  volumeCapacityM3: number;
  refrigerated: boolean;
  hazmatCertified: boolean;
  status: VehicleStatus;
}
