# ADR-017 — SLA Penalty Rates by Tier

**Status:** Accepted

---

## Context

BR-004 states that ETA must not exceed the promised delivery date. When it does, `billing-service` calculates an SLA penalty. The penalty rates by SLA tier are a business decision that must be documented and traceable.

---

## Decision

SLA penalties are calculated as **BRL per late day** with rates varying by tier:

| SLA Tier | Penalty per Late Day |
|----------|----------------------|
| STANDARD | BRL 50.00 |
| PRIORITY | BRL 150.00 |
| EXPRESS  | BRL 300.00 |

Implementation in `SlaPenalty.java`:

```java
private static final Map<SlaType, BigDecimal> DAILY_RATES = Map.of(
    SlaType.STANDARD, new BigDecimal("50.00"),
    SlaType.PRIORITY, new BigDecimal("150.00"),
    SlaType.EXPRESS,  new BigDecimal("300.00")
);
```

Penalty applies only when `actualDeliveryDate` is strictly after `promisedDeliveryDate`. Zero or negative late days → no penalty.

The penalty is added to the invoice total in `Invoice.generate()` if `slaPenalty.applies()`.

---

## Consequences

- Rates are hardcoded constants in the domain — easy to find and change, but requires a redeploy
- Moving rates to a configuration file or a `billing-config` table is recommended before production so business can adjust without a code change
- All monetary values use `BigDecimal` with `HALF_UP` rounding at 2 decimal places (see [ADR-018](ADR-018-bigdecimal-money.md))
