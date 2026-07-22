package com.github.marcelorodrigo.dutytracker.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** A percentage represented in percentage points, where 100 means one whole. */
public record Percentage(BigDecimal value) implements Comparable<Percentage> {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public Percentage {
        Objects.requireNonNull(value, "value is required");
    }

    public static Percentage of(BigDecimal value) {
        return new Percentage(value);
    }

    public BigDecimal asFactor() {
        return value.divide(ONE_HUNDRED);
    }

    public boolean isNegative() {
        return value.signum() < 0;
    }

    public boolean isPositive() {
        return value.signum() > 0;
    }

    @Override
    public int compareTo(Percentage other) {
        return value.compareTo(other.value);
    }
}
