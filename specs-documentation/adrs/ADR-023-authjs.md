# ADR-023 — Auth.js v5 for Authentication and Session Management

**Status:** Accepted

---

## Context

The customer portal is accessed by two actor types with distinct data scopes:
- **Shippers** — see their own shipments and invoices
- **Carriers** — see their own fleet, drivers, and assigned shipments

Authentication must be secure, server-managed, and extensible to Phase 5 multi-tenant OAuth/SAML without a framework rewrite.

---

## Decision

Use **Auth.js v5** (`next-auth`) for authentication.

Configuration:
- **Session strategy:** `jwt` — stateless; session data encoded in a signed httpOnly cookie
- **Providers (Phase 2):** Credentials provider (username + password stored in the platform's user service)
- **Providers (Phase 5):** OAuth 2.0 (Google Workspace, Azure AD) and SAML for enterprise tenants — added as additional Auth.js providers without changing the session model
- **Session payload:** `{ userId, role: 'SHIPPER' | 'CARRIER', tenantId }` — used by the BFF to scope backend queries

### Route protection

All portal pages are protected by Next.js middleware (`middleware.ts`):

```ts
export { auth as middleware } from "@/auth"
export const config = { matcher: ['/((?!api|_next|login).*)'] }
```

Unauthenticated requests redirect to `/login`. The BFF routes validate `getServerSession()` and return 401 if no session exists.

### Data scoping

The BFF forwards `X-User-Id` and `X-Tenant-Id` headers (derived from the session) to each microservice. Microservices use these to filter results to the authenticated actor's data.

---

## Consequences

- Session cookies are httpOnly and Secure — no XSS token theft
- JWT strategy means no session database needed in Phase 2; Phase 5 adds a session store for revocation
- Role-based page visibility: Shippers see `/shipments` and `/billing`; Carriers see `/fleet` and `/warehouse` — enforced in middleware
- Auth.js Credentials provider is not recommended for production without HTTPS and rate limiting on the login endpoint
