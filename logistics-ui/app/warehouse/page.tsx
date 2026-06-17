"use client";

import { useQuery } from "@tanstack/react-query";
import { warehousesApi } from "@/lib/api/warehouses";
import { CapacityGauge } from "@/components/warehouse/CapacityGauge";
import { Skeleton } from "@/components/ui/skeleton";
import Link from "next/link";
import { Warehouse } from "lucide-react";

export default function WarehousePage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["warehouses"],
    queryFn: () => warehousesApi.list(),
    refetchInterval: 30_000,
  });

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Warehouses</h1>

      {isLoading && (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-56 rounded-xl" />)}
        </div>
      )}

      {isError && <p className="text-sm text-red-500">Failed to load warehouses.</p>}

      {data && data.length === 0 && (
        <div className="flex flex-col items-center justify-center py-16 text-gray-400">
          <Warehouse className="h-12 w-12 mb-3" />
          <p className="text-sm">No warehouses registered.</p>
        </div>
      )}

      {data && (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {data.map(w => (
            <Link key={w.warehouseId} href={`/warehouse/${w.warehouseId}`} className="block hover:ring-2 hover:ring-primary/30 rounded-xl transition-shadow">
              <CapacityGauge warehouse={w} />
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
