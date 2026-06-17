"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import type { UserRole } from "@/auth";
import { Package, Truck, Warehouse, Receipt, LayoutDashboard } from "lucide-react";

const SHIPPER_LINKS = [
  { href: "/dashboard",  label: "Dashboard", icon: LayoutDashboard },
  { href: "/shipments",  label: "Shipments", icon: Package },
  { href: "/billing",    label: "Billing",   icon: Receipt },
];

const CARRIER_LINKS = [
  { href: "/dashboard",  label: "Dashboard", icon: LayoutDashboard },
  { href: "/fleet",      label: "Fleet",     icon: Truck },
  { href: "/warehouse",  label: "Warehouses",icon: Warehouse },
];

export function NavBar({ role, name }: { role: UserRole; name: string }) {
  const pathname = usePathname();
  const links = role === "SHIPPER" ? SHIPPER_LINKS : CARRIER_LINKS;

  return (
    <nav className="border-b bg-white shadow-sm">
      <div className="mx-auto flex max-w-7xl items-center gap-6 px-4 py-3 sm:px-6 lg:px-8">
        <span className="text-lg font-bold text-primary">SLN</span>
        <div className="flex gap-1">
          {links.map(({ href, label, icon: Icon }) => (
            <Link
              key={href}
              href={href}
              className={cn(
                "flex items-center gap-1.5 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                pathname.startsWith(href)
                  ? "bg-primary/10 text-primary"
                  : "text-gray-600 hover:bg-gray-100"
              )}
            >
              <Icon className="h-4 w-4" />
              {label}
            </Link>
          ))}
        </div>
        <div className="ml-auto text-sm text-gray-500">
          {name} &bull; {role}
        </div>
      </div>
    </nav>
  );
}
