package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.ReportOvertimeEntryResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OvertimeLinesGrouperTest {

    private static final LocalDate DATE = LocalDate.of(2026, Month.JULY, 7);

    private final OvertimeLinesGrouper grouper = new OvertimeLinesGrouper();

    @Test
    @DisplayName("should sum matching lines and retain distinct incident identifiers")
    void shouldSumMatchingLinesAndRetainDistinctIncidentIdentifiers() {
        // given
        var first = overtimeEntry(10L, new BigDecimal("1"));
        var second = overtimeEntry(20L, new BigDecimal("2"));
        var repeated = overtimeEntry(10L, new BigDecimal("1"));

        // when
        var result = grouper.group(List.of(first, second, repeated));

        // then
        assertThat(result.entries()).singleElement().satisfies(entry -> {
            assertThat(entry.hours()).isEqualByComparingTo(new BigDecimal("4"));
            assertThat(entry.incidentIds()).containsExactly(10L, 20L);
        });
    }

    @Test
    @DisplayName("should keep allowance percentages in separate groups")
    void shouldKeepAllowancePercentagesInSeparateGroups() {
        // given
        var allowance35 = allowanceEntry(10L, new BigDecimal("35"));
        var allowance50 = allowanceEntry(10L, new BigDecimal("50"));

        // when
        var result = grouper.group(List.of(allowance35, allowance50));

        // then
        assertThat(result.entries())
                .extracting(GroupedOvertimeEntryResponse::allowancePercentage)
                .containsExactly(new BigDecimal("35"), new BigDecimal("50"));
    }

    private ReportOvertimeEntryResponse overtimeEntry(Long incidentId, BigDecimal hours) {
        return new ReportOvertimeEntryResponse(
                incidentId,
                "Incident " + incidentId,
                DATE,
                LocalTime.of(20, 0),
                LocalTime.of(21, 0),
                hours,
                null,
                null,
                false);
    }

    private ReportOvertimeEntryResponse allowanceEntry(Long incidentId, BigDecimal percentage) {
        return new ReportOvertimeEntryResponse(
                incidentId,
                "Incident " + incidentId,
                DATE,
                LocalTime.of(20, 0),
                LocalTime.of(21, 0),
                null,
                BigDecimal.ONE,
                percentage,
                true);
    }
}
