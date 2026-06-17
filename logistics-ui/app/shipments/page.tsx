"use client";

import { useQuery } from "@tanstack/react-query";
import { shipmentsApi } from "@/lib/api/shipments";
import { ShipmentStatusBadge } from "@/components/shipments/StatusBadge";
import { SlaBadge } from "@/components/shipments/SlaBadge";
import { Skeleton } from "@/components/ui/skeleton";
import { formatDate } from "@/lib/utils";
import type { ShipmentStatus } from "@/types/shipment";
import Link from "next/link";
import { useState } from "react";
import { Package } from "lucide-react";

const TABS: { label: string; value: ShipmentStatus | "ALL" }[] = [
  { label: "All", value: "ALL" },
  { label: "Created", value: "CREATED" },
  { label: "Scheduled", value: "SCHEDULED" },
  { label: "Assigned", value: "ASSIGNED" },
  { label: "In Transit", value: "IN_TRANSIT" },
  { label: "Delivered", value: "DELIVERED" },
  { label: "Cancelled", value: "CANCELLED" },
];

export default function ShipmentsPage() {
  const [activeTab, setActiveTab] = useState<ShipmentStatus | "ALL">("ALL");

  const { data, isLoading, isError } = useQuery({
    queryKey: ["shipments", activeTab],
    queryFn: () =>
      shipmentsApi.list(activeTab !== "ALL" ? { status: activeTab } : undefined),
    refetchInterval: 15_000,
  });

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Shipments</h1>

      {/* Filter tabs */}
      <div className="flex gap-1 overflow-x-auto border-b pb-px">
        {TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => setActiveTab(tab.value)}
            data-testid="filter-tab"
            data-value={tab.value}
            className={`whitespace-nowrap px-3 py-2 text-sm font-medium rounded-t transition-colors ${
              activeTab === tab.value
                ? "border-b-2 border-primary text-primary"
                : "text-gray-500 hover:text-gray-700"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Table */}
      {isLoading && (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-12 w-full rounded" />
          ))}
        </div>
      )}

      {isError && (
        <p className="text-sm text-red-500">Failed to load shipments. Please try again.</p>
      )}

      {data && data.length === 0 && (
        <div className="flex flex-col items-center justify-center py-16 text-gray-400">
          <Package className="h-12 w-12 mb-3" />
          <p className="text-sm">No shipments found.</p>
        </div>
      )}

      {data && data.length > 0 && (
        <div className="overflow-x-auto rounded-lg border bg-white shadow-sm">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50">
              <tr>
                {["Shipment ID", "Route", "SLA", "Status", "Required By"].map((h) => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {data.map((s) => (
                <tr
                  key={s.shipmentId}
                  data-testid="shipment-row"
                  className="hover:bg-gray-50 cursor-pointer"
                >
                  <td className="px-4 py-3">
                    <Link href={`/shipments/${s.shipmentId}`} className="font-mono text-xs text-primary hover:underline" data-testid="shipment-id">
                      {s.shipmentId.slice(0, 8)}…
                    </Link>
                  </td>
                  <td className="px-4 py-3 text-gray-700">
                    {s.originCity} → {s.destinationCity}
                  </td>
                  <td className="px-4 py-3">
                    <SlaBadge slaType={s.slaType} />
                  </td>
                  <td className="px-4 py-3">
                    <ShipmentStatusBadge status={s.status} />
                  </td>
                  <td className="px-4 py-3 text-gray-500">
                    {formatDate(s.requiredDeliveryDate)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
