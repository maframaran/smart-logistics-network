"use client";

import { useQuery } from "@tanstack/react-query";
import { billingApi } from "@/lib/api/billing";
import { InvoiceStatusBadge } from "@/components/billing/InvoiceStatusBadge";
import { Skeleton } from "@/components/ui/skeleton";
import { formatBrl, formatDate } from "@/lib/utils";
import Link from "next/link";
import { cn } from "@/lib/utils";
import { Receipt } from "lucide-react";

export default function BillingPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["invoices"],
    queryFn: () => billingApi.listInvoices(),
    refetchInterval: 30_000,
  });

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Billing</h1>

      {isLoading && (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => <Skeleton key={i} className="h-12 w-full rounded" />)}
        </div>
      )}

      {isError && <p className="text-sm text-red-500">Failed to load invoices.</p>}

      {data && data.length === 0 && (
        <div className="flex flex-col items-center justify-center py-16 text-gray-400">
          <Receipt className="h-12 w-12 mb-3" />
          <p className="text-sm">No invoices found.</p>
        </div>
      )}

      {data && data.length > 0 && (
        <div className="overflow-x-auto rounded-lg border bg-white shadow-sm">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50">
              <tr>
                {["Invoice", "Shipment", "Amount", "SLA Penalty", "Status", "Due Date"].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {data.map(inv => {
                const hasPenalty = inv.slaPenaltyAmount && inv.slaPenaltyAmount > 0;
                const isOverdue = inv.status === "OVERDUE";
                return (
                  <tr
                    key={inv.invoiceId}
                    data-testid="invoice-row"
                    data-has-penalty={hasPenalty || undefined}
                    className={cn(
                      "cursor-pointer hover:bg-gray-50",
                      hasPenalty && "border-l-4 border-l-red-500",
                      isOverdue && "bg-amber-50"
                    )}
                  >
                    <td className="px-4 py-3">
                      <Link
                        href={`/billing/${inv.invoiceId}`}
                        className="font-mono text-xs text-primary hover:underline"
                        data-testid="invoice-id"
                      >
                        {inv.invoiceId.slice(0, 8)}…
                      </Link>
                    </td>
                    <td className="px-4 py-3 font-mono text-xs text-gray-500">
                      {inv.shipmentId.slice(0, 8)}…
                    </td>
                    <td className="px-4 py-3 font-medium">{formatBrl(inv.totalAmount)}</td>
                    <td className="px-4 py-3">
                      {hasPenalty ? (
                        <span className="text-red-600 font-medium" data-testid="penalty-amount">
                          {formatBrl(inv.slaPenaltyAmount!)}
                        </span>
                      ) : (
                        <span className="text-gray-400">—</span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <InvoiceStatusBadge status={inv.status} />
                    </td>
                    <td className="px-4 py-3 text-gray-500">{formatDate(inv.dueDate)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
