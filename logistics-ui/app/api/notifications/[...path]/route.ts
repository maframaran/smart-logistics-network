import { auth } from "@/auth";
import { proxyToService } from "@/lib/bff";
import { NextRequest, NextResponse } from "next/server";

const SERVICE_URL = process.env.NOTIFICATION_SERVICE_URL ?? "http://localhost:8087";

async function handler(req: NextRequest) {
  const session = await auth();
  if (!session) return NextResponse.json({ status: 401, title: "Unauthorized" }, { status: 401 });
  return proxyToService(req, SERVICE_URL, "/api/v1/notifications");
}

export { handler as GET, handler as POST };
