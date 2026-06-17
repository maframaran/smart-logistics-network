# ADR-019 — Next.js 15 (App Router) for the Customer-Facing UI

**Status:** Accepted

---

## Context

The platform needs a customer-facing web application for Shippers and Carriers (Phase 2 portal, foundation for Phase 5 multi-tenant SaaS). The backend exposes 7 REST services across ports 8081–8087. Options evaluated:

| Option | Routing | Auth | SSR | Phase 5 path |
|--------|---------|------|-----|--------------|
| Plain React (Vite) | External (React Router) | External (manual) | No | Requires rewrite |
| Vue 3 + Nuxt | Built-in | External | Yes | Smaller ecosystem |
| **Next.js 15 App Router** | **Built-in** | **Auth.js native** | **Yes** | **Direct extension** |
| Angular | Built-in | External | Yes | Overkill for this scope |

---

## Decision

Use **Next.js 15 with the App Router** as the UI framework.

Key reasons:
- **File-based routing** eliminates React Router configuration for all portal pages
- **API Routes** enable a BFF pattern (see [ADR-020](ADR-020-bff-pattern.md)) — browser never reaches microservices directly
- **Server Components** allow shipment status pages to be rendered server-side, improving first-load performance for customers
- **Auth.js** (formerly NextAuth) integrates natively with Next.js — session management, JWT, and extensible to OAuth 2.0 / SAML for Phase 5 tenant isolation
- **React ecosystem** — largest pool of logistics/dashboard component libraries (shadcn/ui, Recharts, TanStack Table)

---

## Consequences

- Node.js runtime required in the Docker stack (new container: `logistics-ui` on port 3000)
- Server Components and Client Components must be deliberately separated — data fetching lives in Server Components; interactive state in `"use client"` components
- Next.js App Router is React 18+ only — no IE11 support (acceptable for a B2B logistics portal)
- Future upgrade to Next.js 16/17 is a minor dependency bump, not an architecture change
