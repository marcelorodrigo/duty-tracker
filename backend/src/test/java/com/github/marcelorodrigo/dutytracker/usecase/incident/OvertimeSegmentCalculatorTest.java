package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OvertimeSegmentCalculatorTest {

    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(17, 0);

    private final OvertimeSegmentCalculator calculator = new OvertimeSegmentCalculator();

    @Test
    @DisplayName("should retain only portions outside working hours")
    void shouldRetainOnlyPortionsOutsideWorkingHours() {
        // given
        var incident = incidentFrom(LocalTime.of(8, 15), LocalTime.of(18, 30));

        // when
        var result = calculator.calculate(incident, WORK_START, WORK_END, false);

        // then
        assertThat(result)
                .extracting(TimeSegment::startMinute, TimeSegment::endMinute)
                .containsExactly(tuple(8 * 60 + 15, 9 * 60), tuple(17 * 60, 18 * 60 + 30));
    }

    @Test
    @DisplayName("should return no overtime when incident stays within working hours")
    void shouldReturnNoOvertimeWhenIncidentStaysWithinWorkingHours() {
        // given
        var incident = incidentFrom(LocalTime.of(10, 0), LocalTime.of(16, 0));

        // when
        var result = calculator.calculate(incident, WORK_START, WORK_END, false);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should retain complete incident on a full-day overtime date")
    void shouldRetainCompleteIncidentOnAFullDayOvertimeDate() {
        // given
        var incident = incidentFrom(LocalTime.of(10, 0), LocalTime.of(16, 0));

        // when
        var result = calculator.calculate(incident, WORK_START, WORK_END, true);

        // then
        assertThat(result)
                .extracting(TimeSegment::startMinute, TimeSegment::endMinute)
                .containsExactly(tuple(10 * 60, 16 * 60));
    }

    @Test
    @DisplayName("should split an overnight segment exactly at midnight")
    void shouldSplitOvernightSegmentExactlyAtMidnight() {
        // given
        var overnightSegment = new TimeSegment(23 * 60, 24 * 60 + 45);

        // when
        var result = calculator.splitAtMidnight(overnightSegment);

        // then
        assertThat(result)
                .extracting(TimeSegment::startMinute, TimeSegment::endMinute)
                .containsExactly(tuple(23 * 60, 24 * 60), tuple(24 * 60, 24 * 60 + 45));
    }

    private static Incident incidentFrom(LocalTime start, LocalTime end) {
        LocalDate date = LocalDate.of(2026, Month.APRIL, 14);
        LocalDateTime startDateTime = LocalDateTime.of(date, start);
        LocalDateTime endDateTime = LocalDateTime.of(end.isAfter(start) ? date : date.plusDays(1), end);
        return new Incident(1L, 1L, "Test incident", startDateTime, endDateTime, LocalDateTime.now());
    }
}
