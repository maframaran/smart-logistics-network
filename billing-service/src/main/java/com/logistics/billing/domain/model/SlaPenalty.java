package com.logistics.billing.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// BR-004: ETA must be ≤ promised delivery date; penalty applies when violated.
public record SlaPenalty(long daysLate, Money penaltyAmount) {

    // Penalty rates per day late by SLA tier
    private static final double STANDARD_RATE_BRL_PER_DAY = 50.0;
    private static final double PRIORITY_RATE_BRL_PER_DAY = 150.0;
    private static final double EXPRESS_RATE_BRL_PER_DAY  = 300.0;

    public SlaPenalty {
        if (daysLate < 0) throw new IllegalArgumentException("daysLate must not be negative");
    }

    public static SlaPenalty calculate(LocalDate promisedDate, LocalDate actualDeliveryDate, SlaType slaType) {
        long daysLate = ChronoUnit.DAYS.between(promisedDate, actualDeliveryDate);
        if (daysLate <= 0) {
            return new SlaPenalty(0, Money.brl(0.0));
        }
        double ratePerDay = switch (slaType) {
            case STANDARD -> STANDARD_RATE_BRL_PER_DAY;
            case PRIORITY -> PRIORITY_RATE_BRL_PER_DAY;
            case EXPRESS  -> EXPRESS_RATE_BRL_PER_DAY;
        };
        return new SlaPenalty(daysLate, Money.brl(ratePerDay * daysLate));
    }

    public boolean applies() {
        return daysLate > 0;
    }
}
