package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.StandbyRateType;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeCalculationContext;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeCalculationContextLoader;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeEntriesCalculator;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GenerateOnCallPeriodReportRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeLinesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodReportResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateOnCallPeriodReportUseCaseTest {

    @Mock
    private OnCallDayEntriesCalculator calculateOnCallDayEntries;

    @Mock
    private OvertimeCalculationContextLoader contextLoader;

    @Mock
    private OvertimeEntriesCalculator overtimeEntriesCalculator;

    @Mock
    private OvertimeLinesGrouper groupOvertimeLines;

    @Mock
    private IncidentGateway incidentGateway;

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private EngineerProfile profile;

    private GenerateOnCallPeriodReportUseCase useCase;
    private OvertimeCalculationContext context;

    private static final Long PERIOD_ID = 1L;
    private static final LocalDateTime PERIOD_START = LocalDateTime.of(2025, 4, 14, 8, 0);
    private static final LocalDateTime PERIOD_END = LocalDateTime.of(2025, 4, 21, 8, 0);
    private static final OnCallPeriod PERIOD =
            new OnCallPeriod(PERIOD_ID, PERIOD_START, PERIOD_END, LocalDateTime.now());

    @BeforeEach
    void setUp() {
        context = new OvertimeCalculationContext(profile, List.of(), Map.of());
        useCase = new GenerateOnCallPeriodReportUseCase(
                calculateOnCallDayEntries,
                contextLoader,
                overtimeEntriesCalculator,
                groupOvertimeLines,
                incidentGateway,
                onCallPeriodGateway);
        // Most tests exercise behavior after the shared calculation context has loaded.
        lenient().when(contextLoader.load(PERIOD_ID)).thenReturn(context);
        // default stub — groupOvertimeLines returns empty grouped list unless overridden
        lenient().when(groupOvertimeLines.group(any())).thenReturn(new GroupedOvertimeLinesResponse(List.of()));
    }

    @Test
    @DisplayName("should return empty incident summaries when period has no incidents")
    void shouldReturnEmptyIncidentSummariesWhenPeriodHasNoIncidents() {
        // given
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(calculateOnCallDayEntries.calculate(PERIOD, profile, Set.of()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of(sampleDayEntry())));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of());

        // when
        OnCallPeriodReportResponse result = useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

        // then
        assertThat(result.periodId()).isEqualTo(PERIOD_ID);
        assertThat(result.periodStart()).isEqualTo(PERIOD_START);
        assertThat(result.periodEnd()).isEqualTo(PERIOD_END);
        assertThat(result.incidentCount()).isZero();
        assertThat(result.incidentIds()).isEmpty();
        assertThat(result.standbyLines()).hasSize(1);
        assertThat(result.overtimeLines()).isEmpty();
    }

    @Test
    @DisplayName("should return grouped overtime lines when period has one incident")
    void shouldReturnGroupedOvertimeLinesWhenPeriodHasOneIncident() {
        // given
        Incident incident = new Incident(
                10L,
                PERIOD_ID,
                "Prod alert",
                LocalDateTime.of(2025, 4, 15, 22, 0),
                LocalDateTime.of(2025, 4, 15, 23, 0),
                LocalDateTime.now());

        OvertimeEntryResponse baseEntry = new OvertimeEntryResponse(
                10L,
                new BigDecimal("1.0000"),
                null,
                null,
                LocalDate.of(2025, 4, 15),
                LocalTime.of(22, 0),
                LocalTime.of(23, 0),
                false);
        OvertimeEntryResponse allowanceEntry = new OvertimeEntryResponse(
                10L,
                null,
                new BigDecimal("1.0000"),
                new BigDecimal("25"),
                LocalDate.of(2025, 4, 15),
                LocalTime.of(22, 0),
                LocalTime.of(23, 0),
                true);

        GroupedOvertimeEntryResponse groupedBase = new GroupedOvertimeEntryResponse(
                LocalDate.of(2025, 4, 15), false, null, new BigDecimal("1.0000"), List.of(10L));
        GroupedOvertimeEntryResponse groupedAllowance = new GroupedOvertimeEntryResponse(
                LocalDate.of(2025, 4, 15), true, new BigDecimal("25"), new BigDecimal("1.0000"), List.of(10L));

        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(calculateOnCallDayEntries.calculate(PERIOD, profile, Set.of()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of(sampleDayEntry())));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(incident));
        when(overtimeEntriesCalculator.calculate(incident, context))
                .thenReturn(new OvertimeEntriesResponse(10L, List.of(baseEntry, allowanceEntry)));
        when(groupOvertimeLines.group(any()))
                .thenReturn(new GroupedOvertimeLinesResponse(List.of(groupedBase, groupedAllowance)));

        // when
        OnCallPeriodReportResponse result = useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

        // then
        assertThat(result.incidentCount()).isEqualTo(1);
        assertThat(result.incidentIds()).containsExactly(10L);
        assertThat(result.overtimeLines()).hasSize(2);
        assertThat(result.overtimeLines().getFirst().date()).isEqualTo(LocalDate.of(2025, 4, 15));
        assertThat(result.overtimeLines().getFirst().incidentIds()).containsExactly(10L);
    }

    @Test
    @DisplayName("should collect incident IDs and return grouped overtime lines")
    void shouldCollectIncidentIdsAndReturnGroupedOvertimeLines() {
        // given
        Incident incident = new Incident(
                20L,
                PERIOD_ID,
                "DB outage",
                LocalDateTime.of(2025, 4, 16, 5, 0),
                LocalDateTime.of(2025, 4, 16, 8, 0),
                LocalDateTime.now());

        OvertimeEntryResponse base1 = new OvertimeEntryResponse(
                20L,
                new BigDecimal("2.0000"),
                null,
                null,
                LocalDate.of(2025, 4, 16),
                LocalTime.of(5, 0),
                LocalTime.of(7, 0),
                false);
        OvertimeEntryResponse base2 = new OvertimeEntryResponse(
                20L,
                new BigDecimal("1.0000"),
                null,
                null,
                LocalDate.of(2025, 4, 16),
                LocalTime.of(7, 0),
                LocalTime.of(8, 0),
                false);
        OvertimeEntryResponse allowance = new OvertimeEntryResponse(
                20L,
                null,
                new BigDecimal("3.0000"),
                new BigDecimal("50"),
                LocalDate.of(2025, 4, 16),
                LocalTime.of(5, 0),
                LocalTime.of(8, 0),
                true);

        GroupedOvertimeEntryResponse groupedBase = new GroupedOvertimeEntryResponse(
                LocalDate.of(2025, 4, 16), false, null, new BigDecimal("3.0000"), List.of(20L));
        GroupedOvertimeEntryResponse groupedAllowance = new GroupedOvertimeEntryResponse(
                LocalDate.of(2025, 4, 16), true, new BigDecimal("50"), new BigDecimal("3.0000"), List.of(20L));

        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(calculateOnCallDayEntries.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(incident));
        when(overtimeEntriesCalculator.calculate(incident, context))
                .thenReturn(new OvertimeEntriesResponse(20L, List.of(base1, base2, allowance)));
        when(groupOvertimeLines.group(any()))
                .thenReturn(new GroupedOvertimeLinesResponse(List.of(groupedBase, groupedAllowance)));

        // when
        OnCallPeriodReportResponse result = useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

        // then
        assertThat(result.incidentIds()).containsExactly(20L);
        assertThat(result.overtimeLines()).hasSize(2);
        assertThat(result.overtimeLines().getFirst().hours()).isEqualByComparingTo(new BigDecimal("3.0000"));
        assertThat(result.overtimeLines().getFirst().incidentIds()).containsExactly(20L);
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when period is absent")
    void shouldThrowInvalidOnCallPeriodExceptionWhenPeriodIsAbsent() {
        // given
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.empty());

        // when / then
        var request = new GenerateOnCallPeriodReportRequest(PERIOD_ID);
        assertThatExceptionOfType(InvalidOnCallPeriodException.class).isThrownBy(() -> useCase.execute(request));
    }

    @Test
    @DisplayName("should propagate ProfileNotFoundException before generating report when profile is absent")
    void shouldPropagateProfileNotFoundExceptionBeforeGeneratingReportWhenProfileIsAbsent() {
        // given
        var request = new GenerateOnCallPeriodReportRequest(PERIOD_ID);
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(contextLoader.load(PERIOD_ID)).thenThrow(new ProfileNotFoundException("EngineerProfile not found"));

        // when / then
        assertThatExceptionOfType(ProfileNotFoundException.class)
                .isThrownBy(() -> useCase.execute(request))
                .withMessage("EngineerProfile not found");
        verifyNoInteractions(calculateOnCallDayEntries, overtimeEntriesCalculator, incidentGateway);
    }

    @Test
    @DisplayName("should list working-hours incident without MyHR overtime lines")
    void shouldListWorkingHoursIncidentWithoutMyHrOvertimeLines() {
        // given
        Incident incident = new Incident(
                30L,
                PERIOD_ID,
                "Working hours call",
                LocalDateTime.of(2025, 4, 15, 10, 0),
                LocalDateTime.of(2025, 4, 15, 11, 0),
                LocalDateTime.now());

        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(calculateOnCallDayEntries.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(incident));
        when(overtimeEntriesCalculator.calculate(incident, context))
                .thenThrow(new IncidentDuringWorkingHoursException());

        // when
        OnCallPeriodReportResponse result = useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

        // then
        assertThat(result.incidentCount()).isEqualTo(1);
        assertThat(result.incidentIds()).containsExactly(30L);
        assertThat(result.overtimeLines()).isEmpty();
    }

    @Test
    @DisplayName("should generate MyHR lines only for incidents outside working hours")
    void shouldGenerateMyHrLinesOnlyForIncidentsOutsideWorkingHours() {
        // given
        Incident workingHoursIncident = new Incident(
                30L,
                PERIOD_ID,
                "Working hours call",
                LocalDateTime.of(2025, 4, 15, 10, 0),
                LocalDateTime.of(2025, 4, 15, 11, 0),
                LocalDateTime.now());

        Incident nightIncident = new Incident(
                31L,
                PERIOD_ID,
                "Night alert",
                LocalDateTime.of(2025, 4, 15, 22, 0),
                LocalDateTime.of(2025, 4, 15, 23, 0),
                LocalDateTime.now());

        OvertimeEntryResponse nightEntry = new OvertimeEntryResponse(
                31L,
                new BigDecimal("1.0000"),
                null,
                null,
                LocalDate.of(2025, 4, 15),
                LocalTime.of(22, 0),
                LocalTime.of(23, 0),
                false);

        GroupedOvertimeEntryResponse groupedNight = new GroupedOvertimeEntryResponse(
                LocalDate.of(2025, 4, 15), false, null, new BigDecimal("1.0000"), List.of(31L));

        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(calculateOnCallDayEntries.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(workingHoursIncident, nightIncident));
        when(overtimeEntriesCalculator.calculate(workingHoursIncident, context))
                .thenThrow(new IncidentDuringWorkingHoursException());
        when(overtimeEntriesCalculator.calculate(nightIncident, context))
                .thenReturn(new OvertimeEntriesResponse(31L, List.of(nightEntry)));
        when(groupOvertimeLines.group(any())).thenReturn(new GroupedOvertimeLinesResponse(List.of(groupedNight)));

        // when
        OnCallPeriodReportResponse result = useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

        // then
        assertThat(result.incidentCount()).isEqualTo(2);
        assertThat(result.incidentIds()).containsExactly(30L, 31L);
        assertThat(result.overtimeLines()).hasSize(1);
        assertThat(result.overtimeLines().getFirst().incidentIds()).containsExactly(31L);
    }

    private OnCallDayEntryResponse sampleDayEntry() {
        return new OnCallDayEntryResponse(
                LocalDate.of(2025, 4, 14),
                "Monday",
                new BigDecimal("10.0000"),
                StandbyRateType.WEEKDAY_SATURDAY,
                false);
    }
}
