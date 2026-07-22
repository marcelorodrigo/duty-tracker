package com.github.marcelorodrigo.dutytracker.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** A monetary amount with arithmetic and output rounding kept in one place. */
public record Money(BigDecimal value) {

    private static final int API_SCALE = 2;
    private static final RoundingMode API_ROUNDING_MODE = RoundingMode.HALF_UP;

    public Money {
        Objects.requireNonNull(value, "value is required");
    }

    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        return new Money(value.add(other.value));
    }

    public Money multiply(Hours hours) {
        return new Money(value.multiply(hours.value()));
    }

    public Money multiply(int factor) {
        return new Money(value.multiply(BigDecimal.valueOf(factor)));
    }

    public Money apply(Percentage percentage) {
        return new Money(value.multiply(percentage.asFactor()).setScale(API_SCALE, API_ROUNDING_MODE));
    }

    public BigDecimal toApiAmount() {
        return value.setScale(API_SCALE, API_ROUNDING_MODE);
    }
}
