import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import type { VehicleListItem, VehicleStatus } from "@/types/vehicle";
import { Truck, Thermometer, AlertTriangle } from "lucide-react";

const STATUS_VARIANT: Record<VehicleStatus, "success" | "info" | "warning" | "muted"> = {
  AVAILABLE:       "success",
  ASSIGNED:        "info",
  MAINTENANCE:     "warning",
  OUT_OF_SERVICE:  "muted",
};

export function VehicleCard({ vehicle }: { vehicle: VehicleListItem }) {
  return (
    <Card
      data-testid="vehicle-card"
      data-status={vehicle.status}
      className="hover:shadow-md transition-shadow"
    >
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Truck className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm font-mono">{vehicle.licensePlate}</CardTitle>
          </div>
          <Badge variant={STATUS_VARIANT[vehicle.status]} data-testid="status-badge">
            {vehicle.status.replace(/_/g, " ")}
          </Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-2 text-sm text-muted-foreground">
        <p>{vehicle.type.replace(/_/g, " ")}</p>
        <p>{vehicle.weightCapacityKg.toLocaleString()} kg &bull; {vehicle.volumeCapacityM3} m³</p>
        <div className="flex gap-2 mt-1">
          {vehicle.refrigerated && (
            <Badge variant="info" data-testid="refrigerated-badge">
              <Thermometer className="h-3 w-3 mr-1" /> Refrigerated
            </Badge>
          )}
          {vehicle.hazmatCertified && (
            <Badge variant="warning" data-testid="hazmat-badge">
              <AlertTriangle className="h-3 w-3 mr-1" /> HAZMAT
            </Badge>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
