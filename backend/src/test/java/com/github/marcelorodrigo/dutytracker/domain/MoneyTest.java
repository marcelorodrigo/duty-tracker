package com.github.marcelorodrigo.dutytracker.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    @DisplayName("should calculate and round percentage earnings in one policy")
    void shouldCalculateAndRoundPercentageEarningsInOnePolicy() {
        // given
        var hourlyRate = Money.of(new BigDecimal("25.00"));
        var hours = new Hours(new BigDecimal("1.3333"));
        var percentage = Percentage.of(new BigDecimal("35"));

        // when
        var earnings = hourlyRate.multiply(hours).apply(percentage);

        // then
        assertThat(earnings.toApiAmount()).isEqualByComparingTo(new BigDecimal("11.67"));
    }

    @Test
    @DisplayName("should add already settled monetary amounts")
    void shouldAddAlreadySettledMonetaryAmounts() {
        // given
        var first = Money.of(new BigDecimal("11.67"));
        var second = Money.of(new BigDecimal("5.36"));

        // when
        var total = Money.zero().add(first).add(second);

        // then
        assertThat(total.toApiAmount()).isEqualByComparingTo(new BigDecimal("17.03"));
    }

    @Test
    @DisplayName("should apply the standby monthly-hours factor before settlement")
    void shouldApplyStandbyMonthlyHoursFactorBeforeSettlement() {
        // given
        var hourlyRate = Money.of(new BigDecimal("25.00"));
        var hours = new Hours(new BigDecimal("2"));
        var percentage = Percentage.of(new BigDecimal("0.067"));

        // when
        var earnings = hourlyRate.multiply(hours).multiply(160).apply(percentage);

        // then
        assertThat(earnings.toApiAmount()).isEqualByComparingTo(new BigDecimal("5.36"));
    }
}
