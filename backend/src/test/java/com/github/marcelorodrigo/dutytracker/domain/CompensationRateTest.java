package com.github.marcelorodrigo.dutytracker.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCompensationRateException;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompensationRateTest {

    @Test
    @DisplayName("should create overtime allowance with its required schedule")
    void shouldCreateOvertimeAllowanceWithItsRequiredSchedule() {
        // given
        var percentage = Percentage.of(new BigDecimal("35.00"));
        var timeFrom = LocalTime.of(18, 0);
        var timeTo = LocalTime.of(22, 0);

        // when
        var rate = CompensationRate.overtimeAllowance(OvertimeDayType.WEEKDAY, "Evening", timeFrom, timeTo, percentage);

        // then
        assertThat(rate.id()).isNull();
        assertThat(rate.rateCategory()).isEqualTo(RateCategory.OVERTIME_ALLOWANCE);
        assertThat(rate.percentage()).isEqualTo(percentage);
    }

    @Test
    @DisplayName("should reject overtime allowance without its complete schedule")
    void shouldRejectOvertimeAllowanceWithoutItsCompleteSchedule() {
        // given
        var percentage = Percentage.of(new BigDecimal("35.00"));
        var timeFrom = LocalTime.of(18, 0);
        var timeTo = LocalTime.of(22, 0);

        // when / then
        assertThatThrownBy(() -> new CompensationRate(
                        null, RateCategory.OVERTIME_ALLOWANCE, null, "Evening", timeFrom, timeTo, percentage))
                .isInstanceOf(InvalidCompensationRateException.class)
                .hasMessage("Overtime allowance rates require overtimeDayType, timeFrom and timeTo");
    }

    @Test
    @DisplayName("should reject schedule fields for a rate without an overtime allowance")
    void shouldRejectScheduleFieldsForRateWithoutOvertimeAllowance() {
        // given
        var percentage = Percentage.of(new BigDecimal("100.00"));
        var timeTo = LocalTime.of(23, 59);

        // when / then
        assertThatThrownBy(() -> new CompensationRate(
                        null, RateCategory.OVERTIME_BASE, null, "Base", LocalTime.MIDNIGHT, timeTo, percentage))
                .isInstanceOf(InvalidCompensationRateException.class)
                .hasMessage("Only overtime allowance rates may define overtimeDayType, timeFrom or timeTo");
    }

    @Test
    @DisplayName("should reject negative percentage")
    void shouldRejectNegativePercentage() {
        // given
        var percentage = Percentage.of(new BigDecimal("-0.01"));

        // when / then
        assertThatThrownBy(() ->
                        new CompensationRate(null, RateCategory.OVERTIME_BASE, null, "Base", null, null, percentage))
                .isInstanceOf(InvalidCompensationRateException.class)
                .hasMessage("percentage must be >= 0");
    }

    @Test
    @DisplayName("should preserve rate identity and schedule when details change")
    void shouldPreserveRateIdentityAndScheduleWhenDetailsChange() {
        // given
        var rate = CompensationRate.overtimeAllowance(
                OvertimeDayType.SATURDAY,
                "Old label",
                LocalTime.of(18, 0),
                LocalTime.of(22, 0),
                Percentage.of(new BigDecimal("35.00")));

        // when
        var updated = rate.withDetails("New label", Percentage.of(new BigDecimal("50.00")));

        // then
        assertThat(updated)
                .extracting(
                        CompensationRate::id,
                        CompensationRate::rateCategory,
                        CompensationRate::overtimeDayType,
                        CompensationRate::timeFrom,
                        CompensationRate::timeTo)
                .containsExactly(
                        rate.id(), rate.rateCategory(), rate.overtimeDayType(), rate.timeFrom(), rate.timeTo());
    }
}
