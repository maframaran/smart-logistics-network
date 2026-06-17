import { Badge } from "@/components/ui/badge";
import type { InvoiceStatus } from "@/types/invoice";

const CONFIG: Record<InvoiceStatus, { label: string; variant: "success" | "warning" | "destructive" | "muted" }> = {
  PENDING:   { label: "Pending",   variant: "warning" },
  PAID:      { label: "Paid",      variant: "success" },
  OVERDUE:   { label: "Overdue",   variant: "destructive" },
  CANCELLED: { label: "Cancelled", variant: "muted" },
};

export function InvoiceStatusBadge({ status }: { status: InvoiceStatus }) {
  const { label, variant } = CONFIG[status];
  return <Badge variant={variant} data-testid="status-badge">{label}</Badge>;
}
