export { auth as middleware } from "@/auth";

export const config = {
  // Protect all routes except login page, Next.js internals, and static assets
  matcher: ["/((?!login|api/auth|_next/static|_next/image|favicon.ico).*)"],
};
