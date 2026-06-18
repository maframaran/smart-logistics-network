import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { InvoiceStatusBadge } from "@/components/billing/InvoiceStatusBadge";
import { formatBrl, formatDate } from "@/lib/utils";
import type { Invoice } from "@/types/invoice";
import { notFound } from "next/navigation";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";

async function getInvoice(id: string): Promise<Invoice | null> {
  try {
    const res = await fetch(
      `${process.env.BILLING_SERVICE_URL}/api/v1/invoices/${id}`,
      { next: { revalidate: 30 } }
    );
    if (res.status === 404) return null;
    if (!res.ok) throw new Error();
    return res.json();
  } catch {
    return null;
  }
}

export default async function InvoiceDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const invoice = await getInvoice(id);
  if (!invoice) notFound();

  const hasPenalty = invoice.slaPenaltyAmount && invoice.slaPenaltyAmount > 0;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Link href="/billing" className="text-gray-400 hover:text-gray-700">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <h1 className="text-xl font-bold font-mono" data-testid="invoice-id">
          {invoice.invoiceId.slice(0, 8)}…
        </h1>
        <InvoiceStatusBadge status={invoice.status} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader><CardTitle>Summary</CardTitle></CardHeader>
          <CardContent className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-xs text-muted-foreground">Shipment</p>
              <p className="font-mono text-xs">{invoice.shipmentId.slice(0, 8)}…</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Issued</p>
              <p>{formatDate(invoice.issuedAt)}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Due Date</p>
              <p>{formatDate(invoice.dueDate)}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Status</p>
              <InvoiceStatusBadge status={invoice.status} />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle>Amount Breakdown</CardTitle></CardHeader>
          <CardContent className="space-y-3 text-sm">
            {invoice.lineItems?.map((item, i) => (
              <div key={i} className="flex justify-between">
                <span className="text-gray-700">{item.description}</span>
                <span className="font-medium">{formatBrl(item.amount)}</span>
              </div>
            ))}
            {hasPenalty && (
              <div className="flex justify-between text-red-600 font-medium border-t pt-2">
                <span>SLA Penalty</span>
                <span data-testid="sla-penalty-amount">{formatBrl(invoice.slaPenaltyAmount!)}</span>
              </div>
            )}
            <div className="flex justify-between font-bold text-base border-t pt-3">
              <span>Total</span>
              <span data-testid="total-amount">{formatBrl(invoice.totalAmount)}</span>
            </div>
          </CardContent>
        </Card>

        {invoice.carrierPayment && (
          <Card className="lg:col-span-2">
            <CardHeader><CardTitle>Carrier Payment</CardTitle></CardHeader>
            <CardContent className="grid grid-cols-3 gap-4 text-sm">
              <div>
                <p className="text-xs text-muted-foreground">Carrier ID</p>
                <p className="font-mono text-xs">{invoice.carrierPayment.carrierId}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Amount</p>
                <p>{formatBrl(invoice.carrierPayment.amount)}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Status</p>
                <p>{invoice.carrierPayment.status}</p>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
