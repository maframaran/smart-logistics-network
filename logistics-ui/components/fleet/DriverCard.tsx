import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { hoursBarColor } from "@/lib/utils";
import type { DriverListItem, DriverStatus } from "@/types/driver";
import { User, AlertTriangle } from "lucide-react";

const MAX_HOURS = 9;

const STATUS_VARIANT: Record<DriverStatus, "success" | "info" | "warning" | "muted" | "destructive"> = {
  AVAILABLE:  "success",
  DRIVING:    "info",
  RESTING:    "warning",
  SUSPENDED:  "destructive",
};

export function DriverCard({ driver }: { driver: DriverListItem }) {
  const color = hoursBarColor(driver.hoursToday);
  const pct = Math.min(100, (driver.hoursToday / MAX_HOURS) * 100);

  return (
    <Card
      data-testid="driver-card"
      data-status={driver.status}
      className="hover:shadow-md transition-shadow"
    >
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <User className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm">{driver.name}</CardTitle>
          </div>
          <Badge variant={STATUS_VARIANT[driver.status]} data-testid="status-badge">
            {driver.status}
          </Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-3 text-sm">
        <p className="text-muted-foreground">License: {driver.licenseType}</p>
        {driver.certifications.includes("HAZMAT") && (
          <Badge variant="warning" data-testid="hazmat-badge">
            <AlertTriangle className="h-3 w-3 mr-1" /> HAZMAT
          </Badge>
        )}
        {/* Hours bar — BR-005 */}
        <div className="space-y-1">
          <div className="flex justify-between text-xs text-muted-foreground">
            <span>Daily hours</span>
            <span data-testid="hours-label">{driver.hoursToday.toFixed(1)} / {MAX_HOURS}h</span>
          </div>
          <div className="h-2 w-full rounded-full bg-gray-100">
            <div
              className={`h-2 rounded-full transition-all ${
                color === "red" ? "bg-red-500" : color === "amber" ? "bg-amber-400" : "bg-green-500"
              }`}
              style={{ width: `${pct}%` }}
              role="progressbar"
              aria-valuenow={driver.hoursToday}
              aria-valuemax={MAX_HOURS}
              data-testid="hours-bar"
              data-color={color}
            />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
