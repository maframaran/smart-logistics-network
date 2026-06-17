import { formatDateTime } from "@/lib/utils";
import type { StatusTransition } from "@/types/shipment";
import { CheckCircle } from "lucide-react";

export function StatusTimeline({ history }: { history: StatusTransition[] }) {
  return (
    <ol className="relative border-l border-gray-200 ml-3" data-testid="shipment-timeline">
      {history.map((step, i) => (
        <li key={i} className="mb-6 ml-6" data-testid="timeline-step">
          <span className="absolute -left-3 flex h-6 w-6 items-center justify-center rounded-full bg-green-100 ring-8 ring-white">
            <CheckCircle className="h-4 w-4 text-green-700" />
          </span>
          <h3 className="text-sm font-semibold text-gray-900">{step.status.replace(/_/g, " ")}</h3>
          <time className="text-xs text-gray-500" data-testid="timeline-timestamp">
            {formatDateTime(step.occurredAt)}
          </time>
        </li>
      ))}
    </ol>
  );
}
