import type { Invoice, InvoiceListItem } from "@/types/invoice";

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, init);
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.json() as Promise<T>;
}

export const invoicesApi = {
  list(params?: { status?: string; shipperId?: string }): Promise<InvoiceListItem[]> {
    const qs = new URLSearchParams(params as Record<string, string>).toString();
    return fetchJson<InvoiceListItem[]>(`/api/invoices${qs ? `?${qs}` : ""}`);
  },

  get(id: string): Promise<Invoice> {
    return fetchJson<Invoice>(`/api/invoices/${id}`);
  },

  pay(id: string): Promise<void> {
    return fetchJson<void>(`/api/invoices/${id}/pay`, { method: "POST" });
  },
};
