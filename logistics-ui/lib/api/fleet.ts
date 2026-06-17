import type { Vehicle, VehicleListItem } from "@/types/vehicle";
import type { Driver, DriverListItem } from "@/types/driver";

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, init);
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.json() as Promise<T>;
}

export const vehiclesApi = {
  list(params?: { status?: string; carrierId?: string }): Promise<VehicleListItem[]> {
    const qs = new URLSearchParams(params as Record<string, string>).toString();
    return fetchJson<VehicleListItem[]>(`/api/vehicles${qs ? `?${qs}` : ""}`);
  },

  get(id: string): Promise<Vehicle> {
    return fetchJson<Vehicle>(`/api/vehicles/${id}`);
  },
};

export const driversApi = {
  list(params?: { status?: string; carrierId?: string }): Promise<DriverListItem[]> {
    const qs = new URLSearchParams(params as Record<string, string>).toString();
    return fetchJson<DriverListItem[]>(`/api/drivers${qs ? `?${qs}` : ""}`);
  },

  get(id: string): Promise<Driver> {
    return fetchJson<Driver>(`/api/drivers/${id}`);
  },
};
