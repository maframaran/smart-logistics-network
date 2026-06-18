import { auth } from "@/auth";
import { ShipmentStatusBadge } from "@/components/shipments/StatusBadge";
import { SlaBadge } from "@/components/shipments/SlaBadge";
import { StatusTimeline } from "@/components/shipments/StatusTimeline";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { formatDate, formatBrl } from "@/lib/utils";
import type { Shipment } from "@/types/shipment";
import { notFound } from "next/navigation";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";

async function getShipment(id: string): Promise<Shipment | null> {
  try {
    const res = await fetch(
      `${process.env.SHIPMENT_SERVICE_URL}/api/v1/shipments/${id}`,
      { next: { revalidate: 15 } }
    );
    if (res.status === 404) return null;
    if (!res.ok) throw new Error("fetch failed");
    return res.json();
  } catch {
    return null;
  }
}

async function getRoute(routeId: string | undefined) {
  if (!routeId) return null;
  try {
    const res = await fetch(
      `${process.env.ROUTING_SERVICE_URL}/api/v1/routes/${routeId}`,
      { next: { revalidate: 60 } }
    );
    if (!res.ok) return null;
    return res.json();
  } catch {
    return null;
  }
}

export default async function ShipmentDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const shipment = await getShipment(id);
  if (!shipment) notFound();

  const route = await getRoute(shipment.routeId);
  const isLate = shipment.deliveredAt
    ? new Date(shipment.deliveredAt) > new Date(shipment.requiredDeliveryDate)
    : undefined;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Link href="/shipments" className="text-gray-400 hover:text-gray-700">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <h1 className="text-xl font-bold font-mono" data-testid="shipment-id">
          {shipment.shipmentId.slice(0, 8)}…
        </h1>
        <ShipmentStatusBadge status={shipment.status} />
        <SlaBadge slaType={shipment.slaType} />
        {isLate === false && (
          <Badge variant="success" data-testid="sla-result-badge" data-color="green">On Time</Badge>
        )}
        {isLate === true && (
          <Badge variant="destructive" data-testid="sla-result-badge" data-color="red">Late</Badge>
        )}
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Left column — timeline */}
        <div className="space-y-4">
          <Card>
            <CardHeader><CardTitle>Status Timeline</CardTitle></CardHeader>
            <CardContent>
              <StatusTimeline history={shipment.statusHistory ?? []} />
            </CardContent>
          </Card>
        </div>

        {/* Right columns — detail */}
        <div className="lg:col-span-2 space-y-4">
          <Card>
            <CardHeader><CardTitle>Route</CardTitle></CardHeader>
            <CardContent className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-xs text-muted-foreground">Origin</p>
                <p>{shipment.origin.street}, {shipment.origin.city}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Destination</p>
                <p>{shipment.destination.street}, {shipment.destination.city}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Required by</p>
                <p>{formatDate(shipment.requiredDeliveryDate)}</p>
              </div>
              {shipment.deliveredAt && (
                <div>
                  <p className="text-xs text-muted-foreground">Delivered at</p>
                  <p data-testid="actual-delivery-date">{formatDate(shipment.deliveredAt)}</p>
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle>Cargo</CardTitle></CardHeader>
            <CardContent className="grid grid-cols-2 gap-4 text-sm">
              <div><p className="text-xs text-muted-foreground">Weight</p><p>{shipment.cargo.weightKg} kg</p></div>
              <div><p className="text-xs text-muted-foreground">Volume</p><p>{shipment.cargo.volumeM3} m³</p></div>
              <div><p className="text-xs text-muted-foreground">Hazmat</p><p>{shipment.cargo.requiresHazmat ? "Yes" : "No"}</p></div>
              <div><p className="text-xs text-muted-foreground">Cold Chain</p><p>{shipment.cargo.requiresColdChain ? "Yes" : "No"}</p></div>
            </CardContent>
          </Card>

          {(shipment.vehiclePlate || shipment.driverName) && (
            <Card>
              <CardHeader><CardTitle>Assignment</CardTitle></CardHeader>
              <CardContent className="grid grid-cols-2 gap-4 text-sm">
                {shipment.vehiclePlate && (
                  <div><p className="text-xs text-muted-foreground">Vehicle</p><p data-testid="vehicle-plate" className="font-mono">{shipment.vehiclePlate}</p></div>
                )}
                {shipment.driverName && (
                  <div><p className="text-xs text-muted-foreground">Driver</p><p data-testid="driver-name">{shipment.driverName}</p></div>
                )}
              </CardContent>
            </Card>
          )}

          {route && (
            <Card>
              <CardHeader><CardTitle>Route Summary</CardTitle></CardHeader>
              <CardContent className="grid grid-cols-2 gap-4 text-sm">
                <div><p className="text-xs text-muted-foreground">Distance</p><p>{route.totalDistanceKm?.toFixed(1)} km</p></div>
                <div><p className="text-xs text-muted-foreground">Fuel Cost</p><p>{formatBrl(route.fuelEstimateBrl)}</p></div>
                <div><p className="text-xs text-muted-foreground">Toll Cost</p><p>{formatBrl(route.tollEstimateBrl)}</p></div>
              </CardContent>
            </Card>
          )}
          {!route && shipment.routeId && (
            <p className="text-sm text-muted-foreground">Route data unavailable.</p>
          )}
        </div>
      </div>
    </div>
  );
}
