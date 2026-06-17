# ADR-021 — shadcn/ui + Tailwind CSS for UI Components

**Status:** Accepted

---

## Context

The customer portal needs a component library for tables, badges, cards, modals, and form controls. Options evaluated:

| Library | Ownership | Customisation | Bundle |
|---------|-----------|---------------|--------|
| Material UI (MUI) | Dependency | Theme tokens | Large |
| Ant Design | Dependency | Less flexible | Large |
| Chakra UI | Dependency | Good | Medium |
| **shadcn/ui + Tailwind** | **You own the code** | **Full** | **Per-component** |

---

## Decision

Use **shadcn/ui** as the component foundation and **Tailwind CSS** for styling.

shadcn/ui is not a dependency — running `npx shadcn@latest add button` copies the component source into `components/ui/`. The team owns and can modify every primitive. This eliminates:
- Version lock-in (a breaking MUI v5 → v6 migration, for example)
- Overriding design tokens with `!important` hacks
- Large bundle payloads for unused components

Tailwind CSS utility classes keep styling co-located with markup, making component code self-documenting and easy to adjust without hunting through CSS files.

For charts (warehouse capacity gauges, shipment volume trends): **Recharts** — lightweight, React-native, composable.

---

## Consequences

- Each `shadcn/ui` component added with `npx shadcn add` requires a one-time copy into `components/ui/` — small overhead but gives full ownership
- Tailwind requires the `tailwind.config.ts` content paths to include all component files
- No pre-built logistics-specific components — `ShipmentTimeline`, `CapacityGauge`, etc. are built from shadcn/ui primitives in `components/shipments/`, `components/warehouse/` etc.
- Recharts adds ~45 KB gzipped; acceptable for an ops dashboard
