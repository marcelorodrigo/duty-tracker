package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GroupOvertimeLinesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeLinesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.ReportOvertimeEntryResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupOvertimeLinesUseCaseTest {

    private GroupOvertimeLinesUseCase useCase;

    private static final LocalDate DATE_APR_15 = LocalDate.of(2025, 4, 15);
    private static final LocalDate DATE_APR_16 = LocalDate.of(2025, 4, 16);

    @BeforeEach
    void setUp() {
        useCase = new GroupOvertimeLinesUseCase();
    }

    @Test
    @DisplayName("should return empty list when no entries are provided")
    void shouldReturnEmptyListWhenNoEntriesProvided() {
        // given
        GroupOvertimeLinesRequest request = new GroupOvertimeLinesRequest(List.of());

        // when
        GroupedOvertimeLinesResponse result = useCase.execute(request);

        // then
        assertThat(result.entries()).isEmpty();
    }

    @Test
    @DisplayName("should group single-incident entry into one grouped entry with its hours and incidentId")
    void shouldGroupSingleIncidentEntryIntoOneGroupedEntry() {
        // given
        ReportOvertimeEntryResponse entry = overtimeEntry(10L, DATE_APR_15, new BigDecimal("2"), null, false);
        GroupOvertimeLinesRequest request = new GroupOvertimeLinesRequest(List.of(entry));

        // when
        GroupedOvertimeLinesResponse result = useCase.execute(request);

        // then
        assertThat(result.entries()).hasSize(1);
        GroupedOvertimeEntryResponse grouped = result.entries().getFirst();
        assertThat(grouped.date()).isEqualTo(DATE_APR_15);
        assertThat(grouped.isAllowanceEntry()).isFalse();
        assertThat(grouped.allowancePercentage()).isNull();
        assertThat(grouped.hours()).isEqualByComparingTo(new BigDecimal("2"));
        assertThat(grouped.incidentIds()).containsExactly(10L);
    }

    @Test
    @DisplayName("should sum hours when multiple entries from the same incident share the same date and option")
    void shouldSumHoursWhenMultipleEntriesFromSameIncidentShareSameDateAndOption() {
        // given — two segments from incident 10 on the same date, both non-allowance
        ReportOvertimeEntryResponse seg1 = overtimeEntry(10L, DATE_APR_15, new BigDecimal("1"), null, false);
        ReportOvertimeEntryResponse seg2 = overtimeEntry(10L, DATE_APR_15, new BigDecimal("1"), null, false);
        GroupOvertimeLinesRequest request = new GroupOvertimeLinesRequest(List.of(seg1, seg2));

        // when
        GroupedOvertimeLinesResponse result = useCase.execute(request);

        // then
        assertThat(result.entries()).hasSize(1);
        GroupedOvertimeEntryResponse grouped = result.entries().getFirst();
        assertThat(grouped.hours()).isEqualByComparingTo(new BigDecimal("2"));
        assertThat(grouped.incidentIds()).containsExactly(10L);
    }

    @Test
    @DisplayName("should merge entries from two incidents with the same date and option, collecting both incidentIds")
    void shouldMergeEntriesFromTwoIncidentsWithSameDateAndOption() {
        // given — two different incidents on the same date, same non-allowance option
        ReportOvertimeEntryResponse entry1 = overtimeEntry(10L, DATE_APR_15, new BigDecimal("1"), null, false);
        ReportOvertimeEntryResponse entry2 = overtimeEntry(20L, DATE_APR_15, new BigDecimal("2"), null, false);
        GroupOvertimeLinesRequest request = new GroupOvertimeLinesRequest(List.of(entry1, entry2));

        // when
        GroupedOvertimeLinesResponse result = useCase.execute(request);

        // then
        assertThat(result.entries()).hasSize(1);
        GroupedOvertimeEntryResponse grouped = result.entries().getFirst();
        assertThat(grouped.hours()).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(grouped.incidentIds()).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("should produce separate grouped entries when same date has different allowance percentages")
    void shouldProduceSeparateGroupedEntriesForDifferentAllowancePercentagesOnSameDate() {
        // given — two allowance entries on the same date but different percentages
        ReportOvertimeEntryResponse entry25 =
                allowanceEntry(10L, DATE_APR_15, new BigDecimal("1"), new BigDecimal("25"));
        ReportOvertimeEntryResponse entry50 =
                allowanceEntry(10L, DATE_APR_15, new BigDecimal("2"), new BigDecimal("50"));
        GroupOvertimeLinesRequest request = new GroupOvertimeLinesRequest(List.of(entry25, entry50));

        // when
        GroupedOvertimeLinesResponse result = useCase.execute(request);

        // then
        assertThat(result.entries()).hasSize(2);
        assertThat(result.entries())
                .extracting(GroupedOvertimeEntryResponse::allowancePercentage)
                .containsExactlyInAnyOrder(new BigDecimal("25"), new BigDecimal("50"));
    }

    @Test
    @DisplayName(
            "should produce separate grouped entries when midnight-spanning incident generates entries on two dates")
    void shouldProduceSeparateGroupedEntriesForMidnightSpanningIncident() {
        // given — incident spanning midnight: one entry on Apr 15 and one on Apr 16
        ReportOvertimeEntryResponse entryDay1 = overtimeEntry(10L, DATE_APR_15, new BigDecimal("1"), null, false);
        ReportOvertimeEntryResponse entryDay2 = overtimeEntry(10L, DATE_APR_16, new BigDecimal("2"), null, false);
        GroupOvertimeLinesRequest request = new GroupOvertimeLinesRequest(List.of(entryDay1, entryDay2));

        // when
        GroupedOvertimeLinesResponse result = useCase.execute(request);

        // then
        assertThat(result.entries()).hasSize(2);
        GroupedOvertimeEntryResponse day1 = result.entries().get(0);
        GroupedOvertimeEntryResponse day2 = result.entries().get(1);
        assertThat(day1.date()).isEqualTo(DATE_APR_15);
        assertThat(day1.hours()).isEqualByComparingTo(new BigDecimal("1"));
        assertThat(day2.date()).isEqualTo(DATE_APR_16);
        assertThat(day2.hours()).isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    @DisplayName("should not duplicate incidentId when same incident contributes multiple segments to a group")
    void shouldNotDuplicateIncidentIdForSameIncident() {
        // given — two segments from the same incident on the same date (e.g. two rate-band segments)
        ReportOvertimeEntryResponse seg1 = overtimeEntry(42L, DATE_APR_15, new BigDecimal("1"), null, false);
        ReportOvertimeEntryResponse seg2 = overtimeEntry(42L, DATE_APR_15, new BigDecimal("1"), null, false);
        GroupOvertimeLinesRequest request = new GroupOvertimeLinesRequest(List.of(seg1, seg2));

        // when
        GroupedOvertimeLinesResponse result = useCase.execute(request);

        // then
        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().getFirst().incidentIds()).containsExactly(42L);
    }

    @Test
    @DisplayName(
            "two ceiling-rounded segments each of 1 hour sum to 2, not re-rounded — spec: GroupedOvertimeEntryResponse.hours is additive")
    void twoOneHourCeilingRoundedSegmentsSumToTwo() {
        // given
        // Both 31-minute segments have already been ceiling-rounded to 1h upstream.
        // Grouping MUST sum them (→ 2h), not re-round the combined 62 minutes (→ 1h).
        ReportOvertimeEntryResponse seg1 = overtimeEntry(10L, DATE_APR_15, new BigDecimal("1"), null, false);
        ReportOvertimeEntryResponse seg2 = overtimeEntry(10L, DATE_APR_15, new BigDecimal("1"), null, false);

        // when
        GroupedOvertimeLinesResponse result = useCase.execute(new GroupOvertimeLinesRequest(List.of(seg1, seg2)));

        // then
        assertThat(result.entries().getFirst().hours()).isEqualByComparingTo(new BigDecimal("2"));
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private ReportOvertimeEntryResponse overtimeEntry(
            Long incidentId, LocalDate date, BigDecimal hours, BigDecimal allowancePercentage, boolean isAllowance) {
        return new ReportOvertimeEntryResponse(
                incidentId,
                "Incident " + incidentId,
                date,
                LocalTime.of(22, 0),
                LocalTime.of(23, 0),
                isAllowance ? null : hours,
                isAllowance ? hours : null,
                allowancePercentage,
                isAllowance);
    }

    private ReportOvertimeEntryResponse allowanceEntry(
            Long incidentId, LocalDate date, BigDecimal allowanceHours, BigDecimal allowancePercentage) {
        return new ReportOvertimeEntryResponse(
                incidentId,
                "Incident " + incidentId,
                date,
                LocalTime.of(22, 0),
                LocalTime.of(23, 0),
                null,
                allowanceHours,
                allowancePercentage,
                true);
    }
}
