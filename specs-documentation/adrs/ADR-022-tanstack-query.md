# ADR-022 — TanStack Query for Server-State Management

**Status:** Accepted

---

## Context

The portal pages fetch data from 7 microservices. Without a data-fetching layer:
- Every component re-fetches on mount, causing redundant network calls
- Loading and error states must be hand-rolled per component
- Background refresh (shipment status polling) requires manual `setInterval` management
- Cache invalidation after mutations (assign shipment → refetch list) is error-prone

Options: Redux Toolkit Query, SWR, Zustand + fetch, **TanStack Query**.

---

## Decision

Use **TanStack Query v5** (`@tanstack/react-query`) for all server-state fetching in Client Components.

Key features used:
- `useQuery` — fetches and caches list/detail data; automatic background refetch on window focus
- `useMutation` — wraps create/assign/cancel operations; `onSuccess` triggers `invalidateQueries` to refresh the relevant list
- `staleTime` — shipment lists: 30s; fleet status: 60s; invoices: 5 min
- `refetchInterval` — shipment detail page: 15s polling for status changes

Server Components (Next.js RSC) fetch directly without TanStack Query — TanStack Query is used only in `"use client"` components that need interactivity or polling.

---

## Consequences

- No Redux store required — server state lives in TanStack Query cache; UI state (selected tab, open modal) lives in `useState`
- Cache keys follow the pattern `['shipments']`, `['shipment', id]`, `['vehicles', filters]` — documented in `lib/query-client.ts`
- Mutation callbacks handle optimistic updates for the shipment status badge on the list page
- Bundle cost: ~13 KB gzipped — negligible for a dashboard app
