// Shared BFF proxy helper — used by all /app/api/*/[...path]/route.ts files.
// Forwards the incoming Next.js request to the target microservice and returns
// the response unchanged. Auth validation happens before this is called.

import { NextRequest, NextResponse } from "next/server";

export async function proxyToService(
  req: NextRequest,
  serviceBaseUrl: string,
  pathPrefix: string // e.g. "/api/v1/shipments"
): Promise<NextResponse> {
  const incomingPath = req.nextUrl.pathname.replace(/^\/api\/[^/]+/, "");
  const targetUrl = `${serviceBaseUrl}${pathPrefix}${incomingPath}${req.nextUrl.search}`;

  const headers = new Headers(req.headers);
  // Strip Next.js internal headers before forwarding
  headers.delete("host");
  headers.delete("x-forwarded-host");

  try {
    const upstream = await fetch(targetUrl, {
      method: req.method,
      headers,
      body: req.method !== "GET" && req.method !== "HEAD" ? req.body : undefined,
      duplex: "half",
    } as RequestInit);

    const responseHeaders = new Headers(upstream.headers);
    responseHeaders.delete("transfer-encoding"); // chunked encoding not needed

    return new NextResponse(upstream.body, {
      status: upstream.status,
      headers: responseHeaders,
    });
  } catch {
    return NextResponse.json(
      { title: "Service Unavailable", status: 503, detail: `Could not reach ${serviceBaseUrl}` },
      { status: 503 }
    );
  }
}
