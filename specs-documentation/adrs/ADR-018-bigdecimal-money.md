# ADR-018 — BigDecimal for Monetary Values

**Status:** Accepted

---

## Context

`billing-service` handles invoice amounts, carrier payments, and SLA penalties. Using `double` or `float` for money causes floating-point rounding errors (e.g., `0.1 + 0.2 ≠ 0.3`) that accumulate and produce incorrect totals.

---

## Decision

All monetary values in `billing-service` are represented by the `Money` record:

```java
public record Money(BigDecimal amount, String currency) {
    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (currency.isBlank()) throw new IllegalArgumentException("currency must not be blank");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new IllegalArgumentException("Currency mismatch");
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

Rules:
- Scale is always 2 decimal places, rounded `HALF_UP`
- `add()` enforces currency homogeneity — mixing currencies throws immediately
- Persistence stores `amount` as `DECIMAL(19,2)` in SQL

---

## Consequences

- No floating-point rounding errors in financial calculations
- Currency mismatch caught at runtime — avoids silent cross-currency addition
- `BigDecimal` is slower than primitives; acceptable for billing (low-volume calculations)
- Multi-currency support deferred to Phase 5; for now all amounts are BRL
