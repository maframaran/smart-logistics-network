import { Badge } from "@/components/ui/badge";
import type { SlaType } from "@/types/shipment";

const SLA_CONFIG: Record<SlaType, { label: string; variant: "muted" | "warning" | "destructive" }> = {
  STANDARD: { label: "Standard", variant: "muted" },
  PRIORITY: { label: "Priority", variant: "warning" },
  EXPRESS:  { label: "Express",  variant: "destructive" },
};

export function SlaBadge({ slaType }: { slaType: SlaType }) {
  const { label, variant } = SLA_CONFIG[slaType];
  return <Badge variant={variant} data-testid="sla-badge">{label}</Badge>;
}
