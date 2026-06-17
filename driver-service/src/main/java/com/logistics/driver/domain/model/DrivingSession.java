package com.logistics.driver.domain.model;

import java.time.Duration;
import java.time.LocalDate;

// Tracks driving hours for a single calendar day — enforces BR-005
public record DrivingSession(LocalDate date, Duration hoursWorked) {

    private static final Duration MAX_DAILY_HOURS = Duration.ofHours(9);

    public DrivingSession {
        if (date == null) throw new IllegalArgumentException("date must not be null");
        if (hoursWorked == null || hoursWorked.isNegative()) throw new IllegalArgumentException("hoursWorked must not be negative");
    }

    public static DrivingSession startingOn(LocalDate date) {
        return new DrivingSession(date, Duration.ZERO);
    }

    public DrivingSession addHours(Duration additional) {
        return new DrivingSession(date, hoursWorked.plus(additional));
    }

    public boolean wouldExceedLimit(Duration additional) {
        return hoursWorked.plus(additional).compareTo(MAX_DAILY_HOURS) > 0;
    }

    public boolean exceedsLimit() {
        return hoursWorked.compareTo(MAX_DAILY_HOURS) > 0;
    }
}
