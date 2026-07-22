package com.github.marcelorodrigo.dutytracker.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** A duration expressed in hours using the precision exposed by the application API. */
public record Hours(BigDecimal value) {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public Hours {
        Objects.requireNonNull(value, "value is required");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("hours must be >= 0");
        }
        value = value.setScale(SCALE, ROUNDING_MODE);
    }

    public static Hours fromMinutes(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes must be >= 0");
        }
        return new Hours(BigDecimal.valueOf(minutes).divide(MINUTES_PER_HOUR, SCALE, ROUNDING_MODE));
    }

    public static Hours roundedUpFromMinutes(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes must be >= 0");
        }
        int roundedHours =
                Math.max(1, (minutes + MINUTES_PER_HOUR.intValueExact() - 1) / MINUTES_PER_HOUR.intValueExact());
        return new Hours(BigDecimal.valueOf(roundedHours));
    }

    public static Hours zero() {
        return new Hours(BigDecimal.ZERO);
    }

    public Hours add(Hours other) {
        return new Hours(value.add(other.value));
    }

    public boolean isPositive() {
        return value.signum() > 0;
    }
}
