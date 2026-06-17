export type DriverStatus = "AVAILABLE" | "DRIVING" | "RESTING" | "SUSPENDED";

export type LicenseType = "B" | "C" | "CE" | "D" | "E";

export type Certification = "HAZMAT" | "ADR" | "REFRIGERATED_TRANSPORT";

export interface DrivingSession {
  date: string; // ISO-8601 date
  durationHours: number;
}

export interface Driver {
  driverId: string;
  carrierId: string;
  name: string;
  licenseNumber: string;
  licenseType: LicenseType;
  certifications: Certification[];
  status: DriverStatus;
  drivingSessions: DrivingSession[];
  hoursToday: number;
  createdAt: string;
}

export interface DriverListItem {
  driverId: string;
  name: string;
  licenseType: LicenseType;
  certifications: Certification[];
  status: DriverStatus;
  hoursToday: number;
}
