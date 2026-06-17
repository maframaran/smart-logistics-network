"use client";

import { QueryClient } from "@tanstack/react-query";

// Cache key conventions:
//   ['shipments']           — shipment list
//   ['shipment', id]        — single shipment
//   ['vehicles']            — vehicle list
//   ['vehicle', id]         — single vehicle
//   ['drivers']             — driver list
//   ['driver', id]          — single driver
//   ['warehouses']          — warehouse list
//   ['warehouse', id]       — single warehouse
//   ['invoices']            — invoice list
//   ['invoice', id]         — single invoice

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,         // 30s default stale time
      retry: 2,
      refetchOnWindowFocus: true,
    },
  },
});
