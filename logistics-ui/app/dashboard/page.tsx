import { auth } from "@/auth";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Package, Truck, Warehouse, Receipt } from "lucide-react";
import Link from "next/link";

async function fetchSafe<T>(url: string): Promise<T | null> {
  try {
    const res = await fetch(url, { next: { revalidate: 30 } });
    if (!res.ok) return null;
    return res.json();
  } catch {
    return null;
  }
}

export default async function DashboardPage() {
  const session = await auth();
  const role = session?.user.role;

  const [shipments, vehicles, warehouses, invoices] = await Promise.all([
    role === "SHIPPER" ? fetchSafe<unknown[]>(
      `${process.env.SHIPMENT_SERVICE_URL}/api/v1/shipments?shipperId=${session?.user.id}`
    ) : null,
    role === "CARRIER" ? fetchSafe<unknown[]>(
      `${process.env.FLEET_SERVICE_URL}/api/v1/vehicles?carrierId=${session?.user.id}`
    ) : null,
    role === "CARRIER" ? fetchSafe<unknown[]>(
      `${process.env.WAREHOUSE_SERVICE_URL}/api/v1/warehouses`
    ) : null,
    role === "SHIPPER" ? fetchSafe<unknown[]>(
      `${process.env.BILLING_SERVICE_URL}/api/v1/invoices?shipperId=${session?.user.id}&status=PENDING`
    ) : null,
  ]);

  const stats = [
    ...(role === "SHIPPER" ? [
      {
        label: "Active Shipments",
        value: shipments?.length ?? "—",
        icon: Package,
        href: "/shipments",
        error: shipments === null,
        testId: "Active Shipments",
      },
      {
        label: "Outstanding Invoices",
        value: invoices?.length ?? "—",
        icon: Receipt,
        href: "/billing",
        error: invoices === null,
        testId: "Outstanding Invoices",
      },
    ] : []),
    ...(role === "CARRIER" ? [
      {
        label: "Available Vehicles",
        value: (vehicles as Array<{ status: string }> | null)?.filter(v => v.status === "AVAILABLE").length ?? "—",
        icon: Truck,
        href: "/fleet",
        error: vehicles === null,
        testId: "Available Vehicles",
      },
      {
        label: "Warehouses",
        value: warehouses?.length ?? "—",
        icon: Warehouse,
        href: "/warehouse",
        error: warehouses === null,
        testId: "Warehouses",
      },
    ] : []),
  ];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">
        Welcome, {session?.user.name}
      </h1>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <Link key={stat.label} href={stat.href}>
            <Card
              className="hover:shadow-md transition-shadow cursor-pointer"
              data-testid="stat-card"
              data-error={stat.error || undefined}
            >
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {stat.label}
                </CardTitle>
                <stat.icon className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                {stat.error ? (
                  <div data-testid="error-state" className="text-sm text-red-500">
                    Service unavailable
                    <button className="ml-2 text-xs underline" onClick={() => window.location.reload()}>
                      Retry
                    </button>
                  </div>
                ) : (
                  <p className="text-3xl font-bold" data-testid="stat-value">
                    {stat.value}
                  </p>
                )}
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
