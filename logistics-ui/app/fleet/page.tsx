"use client";

import { useQuery } from "@tanstack/react-query";
import { vehiclesApi, driversApi } from "@/lib/api/fleet";
import { VehicleCard } from "@/components/fleet/VehicleCard";
import { DriverCard } from "@/components/fleet/DriverCard";
import { Skeleton } from "@/components/ui/skeleton";
import { Truck, User } from "lucide-react";

export default function FleetPage() {
  const vehicles = useQuery({
    queryKey: ["vehicles"],
    queryFn: () => vehiclesApi.list(),
    refetchInterval: 30_000,
  });

  const drivers = useQuery({
    queryKey: ["drivers"],
    queryFn: () => driversApi.list(),
    refetchInterval: 30_000,
  });

  return (
    <div className="space-y-8">
      <h1 className="text-2xl font-bold">Fleet Board</h1>

      <section>
        <div className="mb-3 flex items-center gap-2">
          <Truck className="h-5 w-5 text-muted-foreground" />
          <h2 className="text-lg font-semibold">Vehicles</h2>
          {vehicles.data && (
            <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-500">
              {vehicles.data.filter(v => v.status === "AVAILABLE").length} available / {vehicles.data.length} total
            </span>
          )}
        </div>

        {vehicles.isLoading && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-36 rounded-xl" />)}
          </div>
        )}
        {vehicles.isError && <p className="text-sm text-red-500">Failed to load vehicles.</p>}
        {vehicles.data && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {vehicles.data.map(v => <VehicleCard key={v.vehicleId} vehicle={v} />)}
          </div>
        )}
      </section>

      <section>
        <div className="mb-3 flex items-center gap-2">
          <User className="h-5 w-5 text-muted-foreground" />
          <h2 className="text-lg font-semibold">Drivers</h2>
          {drivers.data && (
            <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-500">
              {drivers.data.filter(d => d.status === "AVAILABLE").length} available / {drivers.data.length} total
            </span>
          )}
        </div>

        {drivers.isLoading && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-36 rounded-xl" />)}
          </div>
        )}
        {drivers.isError && <p className="text-sm text-red-500">Failed to load drivers.</p>}
        {drivers.data && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {drivers.data.map(d => <DriverCard key={d.driverId} driver={d} />)}
          </div>
        )}
      </section>
    </div>
  );
}
