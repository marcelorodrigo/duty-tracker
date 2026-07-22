package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Money;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
import com.github.marcelorodrigo.dutytracker.domain.StandbyRateType;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OnCallDayEntriesCalculatorTest {

    private static final EngineerProfile PROFILE = new EngineerProfile(
            1L,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            Money.of(new BigDecimal("50.00")),
            Percentage.of(new BigDecimal("0.067")),
            Percentage.of(new BigDecimal("0.084")),
            LocalDateTime.of(2026, Month.JULY, 1, 12, 0));

    private final OnCallDayEntriesCalculator calculator = new OnCallDayEntriesCalculator();

    @Test
    @DisplayName("should calculate partial boundaries and cap a full working day")
    void shouldCalculatePartialBoundariesAndCapFullWorkingDay() {
        // given
        var period = new OnCallPeriod(
                10L,
                LocalDateTime.of(2026, Month.JULY, 6, 20, 0),
                LocalDateTime.of(2026, Month.JULY, 8, 8, 0),
                LocalDateTime.of(2026, Month.JULY, 1, 12, 0));

        // when
        var result = calculator.calculate(period, PROFILE, Set.of());

        // then
        assertThat(result.periodId()).isEqualTo(10L);
        assertThat(result.entries())
                .extracting(entry -> entry.hours().stripTrailingZeros().toPlainString())
                .containsExactly("4", "15", "8");
        assertThat(result.entries()).extracting(entry -> entry.capped()).containsExactly(false, true, false);
    }

    @Test
    @DisplayName("should treat a holiday as a full non-working sunday-rate day")
    void shouldTreatHolidayAsFullNonWorkingSundayRateDay() {
        // given
        var holiday = LocalDate.of(2026, Month.JULY, 7);
        var period = new OnCallPeriod(
                11L,
                LocalDateTime.of(2026, Month.JULY, 6, 20, 0),
                LocalDateTime.of(2026, Month.JULY, 8, 8, 0),
                LocalDateTime.of(2026, Month.JULY, 1, 12, 0));

        // when
        var result = calculator.calculate(period, PROFILE, Set.of(holiday));

        // then
        var holidayEntry = result.entries().get(1);
        assertThat(holidayEntry.date()).isEqualTo(holiday);
        assertThat(holidayEntry.dayLabel()).isEqualTo("Holiday");
        assertThat(holidayEntry.hours()).isEqualByComparingTo(new BigDecimal("24"));
        assertThat(holidayEntry.rateType()).isEqualTo(StandbyRateType.SUNDAY_HOLIDAY);
        assertThat(holidayEntry.capped()).isFalse();
    }
}
