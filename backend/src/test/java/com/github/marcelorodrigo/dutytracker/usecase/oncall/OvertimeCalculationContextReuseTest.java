package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.Money;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeCalculationContext;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeCalculationContextLoader;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeEntriesCalculator;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateEarningsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GenerateOnCallPeriodReportRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.GroupedOvertimeLinesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CalculateEarningsValidator;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OvertimeCalculationContextReuseTest {

    private static final Long PERIOD_ID = 1L;
    private static final OnCallPeriod PERIOD = new OnCallPeriod(
            PERIOD_ID,
            LocalDateTime.of(2026, 7, 1, 8, 0),
            LocalDateTime.of(2026, 7, 8, 8, 0),
            LocalDateTime.of(2026, 6, 1, 12, 0));
    private static final EngineerProfile PROFILE = new EngineerProfile(
            1L,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            Money.of(new BigDecimal("50.00")),
            Percentage.of(new BigDecimal("0.067")),
            Percentage.of(new BigDecimal("0.084")),
            LocalDateTime.of(2026, 6, 1, 12, 0));
    private static final CompensationRate OVERTIME_BASE_RATE = new CompensationRate(
            1L, RateCategory.OVERTIME_BASE, null, "Overtime base", null, null, Percentage.of(new BigDecimal("100")));

    @Mock
    private EngineerProfileGateway engineerProfileGateway;

    @Mock
    private HolidayGateway holidayGateway;

    @Mock
    private CompensationRateGateway compensationRateGateway;

    @Mock
    private IncidentGateway incidentGateway;

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private CalculateOnCallDayEntriesUseCase dayEntriesCalculator;

    @Mock
    private OvertimeEntriesCalculator overtimeEntriesCalculator;

    @Mock
    private GroupOvertimeLinesUseCase groupOvertimeLines;

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    @DisplayName("should load one calculation context as earnings incident count grows")
    void shouldLoadOneCalculationContextAsEarningsIncidentCountGrows(int incidentCount) {
        // given
        var incidents = incidents(incidentCount);
        var contextLoader = contextLoader();
        var useCase = new CalculateEarningsUseCase(
                dayEntriesCalculator,
                contextLoader,
                overtimeEntriesCalculator,
                incidentGateway,
                onCallPeriodGateway,
                compensationRateGateway,
                new CalculateEarningsValidator());
        givenSharedContextData();
        when(compensationRateGateway.findByRateCategory(RateCategory.OVERTIME_BASE))
                .thenReturn(List.of(OVERTIME_BASE_RATE));
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(dayEntriesCalculator.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(incidents);
        when(overtimeEntriesCalculator.calculate(any(Incident.class), any(OvertimeCalculationContext.class)))
                .thenAnswer(invocation -> {
                    Incident incident = invocation.getArgument(0);
                    return new OvertimeEntriesResponse(incident.id(), List.of());
                });

        // when
        useCase.execute(new CalculateEarningsRequest(PERIOD_ID));

        // then
        verify(engineerProfileGateway).find();
        verify(holidayGateway).findByOnCallPeriodId(PERIOD_ID);
        verify(compensationRateGateway).findByRateCategory(RateCategory.OVERTIME_ALLOWANCE);
        verify(overtimeEntriesCalculator, times(incidentCount))
                .calculate(any(Incident.class), any(OvertimeCalculationContext.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    @DisplayName("should load one calculation context as report incident count grows")
    void shouldLoadOneCalculationContextAsReportIncidentCountGrows(int incidentCount) {
        // given
        var incidents = incidents(incidentCount);
        var contextLoader = contextLoader();
        var useCase = new GenerateOnCallPeriodReportUseCase(
                dayEntriesCalculator,
                contextLoader,
                overtimeEntriesCalculator,
                groupOvertimeLines,
                incidentGateway,
                onCallPeriodGateway);
        givenSharedContextData();
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(dayEntriesCalculator.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(incidents);
        when(overtimeEntriesCalculator.calculate(any(Incident.class), any(OvertimeCalculationContext.class)))
                .thenAnswer(invocation -> {
                    Incident incident = invocation.getArgument(0);
                    return new OvertimeEntriesResponse(incident.id(), List.of());
                });
        when(groupOvertimeLines.execute(any())).thenReturn(new GroupedOvertimeLinesResponse(List.of()));

        // when
        useCase.execute(new GenerateOnCallPeriodReportRequest(PERIOD_ID));

        // then
        verify(engineerProfileGateway).find();
        verify(holidayGateway).findByOnCallPeriodId(PERIOD_ID);
        verify(compensationRateGateway).findByRateCategory(RateCategory.OVERTIME_ALLOWANCE);
        verify(overtimeEntriesCalculator, times(incidentCount))
                .calculate(any(Incident.class), any(OvertimeCalculationContext.class));
    }

    private OvertimeCalculationContextLoader contextLoader() {
        return new OvertimeCalculationContextLoader(engineerProfileGateway, holidayGateway, compensationRateGateway);
    }

    private void givenSharedContextData() {
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(holidayGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of());
        when(compensationRateGateway.findByRateCategory(RateCategory.OVERTIME_ALLOWANCE))
                .thenReturn(List.of());
    }

    private List<Incident> incidents(int incidentCount) {
        return IntStream.rangeClosed(1, incidentCount)
                .mapToObj(index -> new Incident(
                        (long) index,
                        PERIOD_ID,
                        "Incident " + index,
                        LocalDateTime.of(2026, 7, 1, 18, 0).plusMinutes(index),
                        LocalDateTime.of(2026, 7, 1, 19, 0).plusMinutes(index),
                        LocalDateTime.of(2026, 7, 1, 20, 0)))
                .toList();
    }
}
