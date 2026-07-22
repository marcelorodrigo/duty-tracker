package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeEntry;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OvertimeEntryCalculatorTest {

    private static final LocalDate INCIDENT_DATE = LocalDate.of(2026, Month.APRIL, 14);

    private final OvertimeEntryCalculator calculator = new OvertimeEntryCalculator(new OvertimeSegmentCalculator());

    @Test
    @DisplayName("should create allowance entries at exact rate transitions")
    void shouldCreateAllowanceEntriesAtExactRateTransitions() {
        // given
        var earlyRate = allowanceRate(LocalTime.of(22, 0), LocalTime.of(23, 0), "50.00");
        var lateRate = allowanceRate(LocalTime.of(23, 0), LocalTime.MIDNIGHT, "75.00");

        // when
        var result = calculator.calculate(
                1L, INCIDENT_DATE, List.of(new TimeSegment(21 * 60 + 30, 24 * 60)), List.of(earlyRate, lateRate));

        // then
        assertThat(result.stream().filter(OvertimeEntry::isAllowanceEntry))
                .extracting(OvertimeEntry::timeFrom, OvertimeEntry::timeTo, OvertimeEntry::allowancePercentage)
                .containsExactly(
                        tuple(LocalTime.of(22, 0), LocalTime.of(23, 0), Percentage.of(new BigDecimal("50.00"))),
                        tuple(LocalTime.of(23, 0), LocalTime.MIDNIGHT, Percentage.of(new BigDecimal("75.00"))));
    }

    @Test
    @DisplayName("should preserve uncovered time as base overtime")
    void shouldPreserveUncoveredTimeAsBaseOvertime() {
        // given
        var rate = allowanceRate(LocalTime.of(22, 0), LocalTime.of(23, 0), "50.00");

        // when
        var result =
                calculator.calculate(1L, INCIDENT_DATE, List.of(new TimeSegment(21 * 60 + 30, 23 * 60)), List.of(rate));

        // then
        assertThat(result.stream()
                        .filter(entry -> !entry.isAllowanceEntry())
                        .filter(entry -> entry.allowancePercentage() == null))
                .extracting(OvertimeEntry::timeFrom, OvertimeEntry::timeTo)
                .contains(tuple(LocalTime.of(21, 30), LocalTime.of(22, 0)));
    }

    @Test
    @DisplayName("should assign the next date to overtime after midnight")
    void shouldAssignTheNextDateToOvertimeAfterMidnight() {
        // given
        var overnightSegment = new TimeSegment(23 * 60, 24 * 60 + 45);

        // when
        var result = calculator.calculate(1L, INCIDENT_DATE, List.of(overnightSegment), List.of());

        // then
        assertThat(result)
                .extracting(OvertimeEntry::date, OvertimeEntry::timeFrom, OvertimeEntry::timeTo)
                .containsExactly(
                        tuple(INCIDENT_DATE, LocalTime.of(23, 0), LocalTime.MIDNIGHT),
                        tuple(INCIDENT_DATE.plusDays(1), LocalTime.MIDNIGHT, LocalTime.of(0, 45)));
    }

    private static CompensationRate allowanceRate(LocalTime from, LocalTime to, String percentage) {
        return new CompensationRate(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Allowance",
                from,
                to,
                Percentage.of(new BigDecimal(percentage)));
    }
}
