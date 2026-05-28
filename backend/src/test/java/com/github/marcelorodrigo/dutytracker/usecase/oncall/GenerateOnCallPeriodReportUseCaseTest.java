package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.StandbyRateType;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.incident.CalculateOvertimeEntriesUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateOnCallDayEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GenerateOnCallPeriodReportRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GroupOvertimeLinesRequest;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateOnCallPeriodReportUseCaseTest {

    @Mock
    private CalculateOnCallDayEntriesUseCase calculateOnCallDayEntries;

    @Mock
    private CalculateOvertimeEntriesUseCase calculateOvertimeEntries;

    @Mock
    private GroupOvertimeLinesUseCase groupOvertimeLines;

    @Mock
    private IncidentGateway incidentGateway;

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private HolidayGateway holidayGateway;

    private GenerateOnCallPeriodReportUseCase useCase;

    private static final Long PERIOD_ID = 1L;
    private static final LocalDateTime PERIOD_START = LocalDateTime.of(2025, 4, 14, 8, 0);
    private static final LocalDateTime PERIOD_END = LocalDateTime.of(2025, 4, 21, 8, 0);
    private static final OnCallPeriod PERIOD =
            new OnCallPeriod(PERIOD_ID, PERIOD_START, PERIOD_END, LocalDateTime.now());

    @BeforeEach
    void setUp() {
        useCase = new GenerateOnCallPeriodReportUseCase(
                calculateOnCallDayEntries,
                calculateOvertimeEntries,
                groupOvertimeLines,
                incidentGateway,
                onCallPeriodGateway,
                holidayGateway);
        // default stub — most tests don't need holidays; override in specific tests
        lenient().when(holidayGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of());
        // default stub — groupOvertimeLines returns empty grouped list unless overridden
        lenient().when(groupOvertimeLines.execute(any())).thenReturn(new GroupedOvertimeLinesResponse(List.of()));
    }

    @Test
    @DisplayName("execute — period with no incidents returns empty summaries and empty overtime lines")
    void periodWithNoIncidents() {
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(calculateOnCallDayEntries.execute(new CalculateOnCallDayEntriesRequest(PERIOD_ID)))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of(sampleDayEntry())));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of());

        OnCallPeriodReportResponse result = useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

        assertThat(result.periodId()).isEqualTo(PERIOD_ID);
        assertThat(result.periodStart()).isEqualTo(PERIOD_START);
        assertThat(result.periodEnd()).isEqualTo(PERIOD_END);
        assertThat(result.incidentCount()).isZero();
        assertThat(result.incidentIds()).isEmpty();
        assertThat(result.standbyLines()).hasSize(1);
        assertThat(result.overtimeLines()).isEmpty();
    }

    @Test
    @DisplayName("execute — period with one incident produces one summary and corresponding grouped overtime lines")
    void periodWithOneIncident() {
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
        when(calculateOnCallDayEntries.execute(new CalculateOnCallDayEntriesRequest(PERIOD_ID)))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of(sampleDayEntry())));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(incident));
        when(calculateOvertimeEntries.execute(new CalculateOvertimeEntriesRequest(10L)))
                .thenReturn(new OvertimeEntriesResponse(10L, List.of(baseEntry, allowanceEntry)));
        when(groupOvertimeLines.execute(any(GroupOvertimeLinesRequest.class)))
                .thenReturn(new GroupedOvertimeLinesResponse(List.of(groupedBase, groupedAllowance)));

        OnCallPeriodReportResponse result = useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

        assertThat(result.incidentCount()).isEqualTo(1);
        assertThat(result.incidentIds()).containsExactly(10L);
        assertThat(result.overtimeLines()).hasSize(2);
        assertThat(result.overtimeLines().getFirst().date()).isEqualTo(LocalDate.of(2025, 4, 15));
        assertThat(result.overtimeLines().getFirst().incidentIds()).containsExactly(10L);
    }

    @Test
    @DisplayName("execute — incident IDs are collected and grouped overtime lines are returned from groupOvertimeLines")
    void incidentIdsCollectedAndGroupedOvertimeLinesBuilt() {
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
        when(calculateOnCallDayEntries.execute(any())).thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(incident));
        when(calculateOvertimeEntries.execute(new CalculateOvertimeEntriesRequest(20L)))
                .thenReturn(new OvertimeEntriesResponse(20L, List.of(base1, base2, allowance)));
        when(groupOvertimeLines.execute(any(GroupOvertimeLinesRequest.class)))
                .thenReturn(new GroupedOvertimeLinesResponse(List.of(groupedBase, groupedAllowance)));

        OnCallPeriodReportResponse result = useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

        assertThat(result.incidentIds()).containsExactly(20L);
        assertThat(result.overtimeLines()).hasSize(2);
        assertThat(result.overtimeLines().getFirst().hours()).isEqualByComparingTo(new BigDecimal("3.0000"));
        assertThat(result.overtimeLines().getFirst().incidentIds()).containsExactly(20L);
    }

    @Test
    @DisplayName("execute — period not found throws InvalidOnCallPeriodException")
    void periodNotFoundThrows() {
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID)))
                .isInstanceOf(InvalidOnCallPeriodException.class);
    }

    @Test
    @DisplayName("execute — incident during working hours is listed but produces no MyHR lines")
    void incidentDuringWorkingHoursExcluded() {
        Incident incident = new Incident(
                30L,
                PERIOD_ID,
                "Working hours call",
                LocalDateTime.of(2025, 4, 15, 10, 0),
                LocalDateTime.of(2025, 4, 15, 11, 0),
                LocalDateTime.now());

        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(calculateOnCallDayEntries.execute(any())).thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(incident));
        when(calculateOvertimeEntries.execute(new CalculateOvertimeEntriesRequest(30L)))
                .thenThrow(new IncidentDuringWorkingHoursException());

        OnCallPeriodReportResponse result = useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

        assertThat(result.incidentCount()).isEqualTo(1);
        assertThat(result.incidentIds()).containsExactly(30L);
        assertThat(result.overtimeLines()).isEmpty();
    }

    @Test
    @DisplayName(
            "execute — mixed incidents (during and outside working hours) all listed but only outside working hours generate MyHR lines")
    void mixedIncidentsFiltered() {
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
        when(calculateOnCallDayEntries.execute(any())).thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(workingHoursIncident, nightIncident));
        when(calculateOvertimeEntries.execute(new CalculateOvertimeEntriesRequest(30L)))
                .thenThrow(new IncidentDuringWorkingHoursException());
        when(calculateOvertimeEntries.execute(new CalculateOvertimeEntriesRequest(31L)))
                .thenReturn(new OvertimeEntriesResponse(31L, List.of(nightEntry)));
        when(groupOvertimeLines.execute(any(GroupOvertimeLinesRequest.class)))
                .thenReturn(new GroupedOvertimeLinesResponse(List.of(groupedNight)));

        OnCallPeriodReportResponse result = useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

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
