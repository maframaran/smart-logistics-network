import type { Warehouse, WarehouseListItem } from "@/types/warehouse";

async function fetchJson<T>(url: string): Promise<T> {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.json() as Promise<T>;
}

export const warehousesApi = {
  list(): Promise<WarehouseListItem[]> {
    return fetchJson<WarehouseListItem[]>("/api/warehouses");
  },

  get(id: string): Promise<Warehouse> {
    return fetchJson<Warehouse>(`/api/warehouses/${id}`);
  },
};
