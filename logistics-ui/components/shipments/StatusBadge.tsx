import { Badge } from "@/components/ui/badge";
import type { ShipmentStatus } from "@/types/shipment";

const STATUS_CONFIG: Record<ShipmentStatus, { label: string; variant: "success" | "info" | "warning" | "destructive" | "muted" | "secondary" }> = {
  DRAFT:      { label: "Draft",       variant: "muted" },
  CREATED:    { label: "Created",     variant: "secondary" },
  SCHEDULED:  { label: "Scheduled",   variant: "info" },
  ASSIGNED:   { label: "Assigned",    variant: "info" },
  PICKED_UP:  { label: "Picked Up",   variant: "warning" },
  IN_TRANSIT: { label: "In Transit",  variant: "warning" },
  DELIVERED:  { label: "Delivered",   variant: "success" },
  CANCELLED:  { label: "Cancelled",   variant: "muted" },
  FAILED:     { label: "Failed",      variant: "destructive" },
  RETURNED:   { label: "Returned",    variant: "muted" },
};

export function ShipmentStatusBadge({ status }: { status: ShipmentStatus }) {
  const { label, variant } = STATUS_CONFIG[status] ?? { label: status, variant: "muted" };
  return <Badge variant={variant} data-testid="status-badge">{label}</Badge>;
}
