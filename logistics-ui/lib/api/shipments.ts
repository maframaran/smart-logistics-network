import type { Shipment, ShipmentListItem } from "@/types/shipment";

const BASE = "/api/shipments";

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, init);
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.json() as Promise<T>;
}

export const shipmentsApi = {
  list(params?: { status?: string; shipperId?: string }): Promise<ShipmentListItem[]> {
    const qs = new URLSearchParams(params as Record<string, string>).toString();
    return fetchJson<ShipmentListItem[]>(`${BASE}${qs ? `?${qs}` : ""}`);
  },

  get(id: string): Promise<Shipment> {
    return fetchJson<Shipment>(`${BASE}/${id}`);
  },

  cancel(id: string, reason: string): Promise<void> {
    return fetchJson<void>(`${BASE}/${id}/cancel`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ reason }),
    });
  },
};
