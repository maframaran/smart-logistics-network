import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CapacityGauge } from "@/components/warehouse/CapacityGauge";
import type { Warehouse, InventoryItem } from "@/types/warehouse";
import { notFound } from "next/navigation";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { cn } from "@/lib/utils";

async function getWarehouse(id: string): Promise<Warehouse | null> {
  try {
    const res = await fetch(
      `${process.env.WAREHOUSE_SERVICE_URL}/api/v1/warehouses/${id}`,
      { next: { revalidate: 30 } }
    );
    if (res.status === 404) return null;
    if (!res.ok) throw new Error();
    return res.json();
  } catch {
    return null;
  }
}

async function getInventory(id: string): Promise<InventoryItem[]> {
  try {
    const res = await fetch(
      `${process.env.WAREHOUSE_SERVICE_URL}/api/v1/warehouses/${id}/inventory`,
      { next: { revalidate: 30 } }
    );
    if (!res.ok) return [];
    return res.json();
  } catch {
    return [];
  }
}

function isExpiringSoon(expirationDate?: string): boolean {
  if (!expirationDate) return false;
  const diff = new Date(expirationDate).getTime() - Date.now();
  return diff > 0 && diff < 7 * 24 * 60 * 60 * 1000; // within 7 days
}

function isExpired(expirationDate?: string): boolean {
  if (!expirationDate) return false;
  return new Date(expirationDate).getTime() < Date.now();
}

export default async function WarehouseDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const [warehouse, inventory] = await Promise.all([
    getWarehouse(id),
    getInventory(id),
  ]);
  if (!warehouse) notFound();

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Link href="/warehouse" className="text-gray-400 hover:text-gray-700">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <h1 className="text-xl font-bold">{warehouse.name}</h1>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div>
          <CapacityGauge label="Weight" current={warehouse.currentWeightKg} max={warehouse.maxWeightKg} unit="kg" />
        </div>

        <div className="lg:col-span-2">
          <Card>
            <CardHeader><CardTitle>Inventory ({inventory.length} SKUs)</CardTitle></CardHeader>
            <CardContent>
              {inventory.length === 0 ? (
                <p className="text-sm text-muted-foreground">No inventory items.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200 text-sm">
                    <thead className="bg-gray-50">
                      <tr>
                        {["SKU", "Description", "Qty", "Weight (kg)", "Volume (m³)", "Expires"].map(h => (
                          <th key={h} className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">
                            {h}
                          </th>
                        ))}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                      {inventory.map(item => {
                        const expired = isExpired(item.expirationDate);
                        const expiring = isExpiringSoon(item.expirationDate);
                        return (
                          <tr
                            key={item.itemId}
                            data-testid="inventory-row"
                            className={cn(
                              expired && "bg-red-50",
                              expiring && !expired && "bg-amber-50"
                            )}
                          >
                            <td className="px-4 py-2 font-mono text-xs">{item.sku}</td>
                            <td className="px-4 py-2">{item.description}</td>
                            <td className="px-4 py-2">{item.quantity}</td>
                            <td className="px-4 py-2">{item.weightKg}</td>
                            <td className="px-4 py-2">{item.volumeM3}</td>
                            <td className={cn("px-4 py-2 text-xs", expired && "text-red-600 font-medium", expiring && "text-amber-600")}>
                              {item.expirationDate
                                ? new Date(item.expirationDate).toLocaleDateString("pt-BR")
                                : "—"}
                              {expired && " (expired)"}
                              {expiring && " (soon)"}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
