package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.Money;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.Percentage;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.StandbyRateType;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeCalculationContext;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeCalculationContextLoader;
import com.github.marcelorodrigo.dutytracker.usecase.incident.OvertimeEntriesCalculator;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateEarningsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.EarningsResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CalculateEarningsValidator;
import java.math.BigDecimal;
import java.time.DayOfWeek;
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
class CalculateEarningsUseCaseTest {

    @Mock
    private CalculateOnCallDayEntriesUseCase calculateOnCallDayEntries;

    @Mock
    private OvertimeCalculationContextLoader contextLoader;

    @Mock
    private OvertimeEntriesCalculator overtimeEntriesCalculator;

    @Mock
    private IncidentGateway incidentGateway;

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private CompensationRateGateway compensationRateGateway;

    private CalculateEarningsUseCase useCase;

    private static final Long PERIOD_ID = 1L;
    private static final LocalDateTime PERIOD_START = LocalDateTime.of(2025, 4, 14, 8, 0);
    private static final LocalDateTime PERIOD_END = LocalDateTime.of(2025, 4, 21, 8, 0);
    private static final OnCallPeriod PERIOD =
            new OnCallPeriod(PERIOD_ID, PERIOD_START, PERIOD_END, LocalDateTime.now());
    private static final BigDecimal HOURLY_RATE = new BigDecimal("25.00");

    // Standby percentages: weekday/sat = 0.067%, sunday/holiday = 0.084%
    // Formula: hours × hourlyRate × 160 × (percentage / 100)
    private static final BigDecimal WEEKDAY_SAT_PCT = new BigDecimal("0.067");
    private static final BigDecimal SUNDAY_HOL_PCT = new BigDecimal("0.084");

    private static final CompensationRate OVERTIME_BASE_RATE = new CompensationRate(
            3L,
            RateCategory.OVERTIME_BASE,
            null,
            "Overtime base rate",
            null,
            null,
            Percentage.of(new BigDecimal("100.0")));

    private EngineerProfile profile() {
        return new EngineerProfile(
                1L,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Money.of(HOURLY_RATE),
                Percentage.of(WEEKDAY_SAT_PCT),
                Percentage.of(SUNDAY_HOL_PCT),
                LocalDateTime.now());
    }

    @BeforeEach
    void setUp() {
        useCase = new CalculateEarningsUseCase(
                calculateOnCallDayEntries,
                contextLoader,
                overtimeEntriesCalculator,
                incidentGateway,
                onCallPeriodGateway,
                compensationRateGateway,
                new CalculateEarningsValidator());
    }

    private OvertimeCalculationContext context(EngineerProfile profile) {
        return new OvertimeCalculationContext(profile, List.of(), Map.of());
    }

    private void stubOvertimeBaseRate() {
        when(compensationRateGateway.findByRateCategory(RateCategory.OVERTIME_BASE))
                .thenReturn(List.of(OVERTIME_BASE_RATE));
    }

    @Test
    @DisplayName("should calculate standby earnings using profile percentages and actual standby hours")
    void shouldCalculateStandbyEarningsWithNoIncidents() {
        // given
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(contextLoader.load(PERIOD_ID)).thenReturn(context(profile()));
        stubOvertimeBaseRate();
        // standby weekday: 2h * 25.00 * 160 * 0.067 / 100 = 5.36
        OnCallDayEntryResponse dayEntry = new OnCallDayEntryResponse(
                LocalDate.of(2025, 4, 14), "Monday", new BigDecimal("2"), StandbyRateType.WEEKDAY_SATURDAY, false);
        when(calculateOnCallDayEntries.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of(dayEntry)));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of());

        // when
        EarningsResponse result = useCase.execute(new CalculateEarningsRequest(PERIOD_ID));

        // then
        assertThat(result.periodId()).isEqualTo(PERIOD_ID);
        assertThat(result.periodStart()).isEqualTo(PERIOD_START);
        assertThat(result.periodEnd()).isEqualTo(PERIOD_END);
        assertThat(result.standbyLines()).hasSize(1);
        assertThat(result.standbyLines().getFirst().compensationLabel()).isEqualTo("On-call Monday\u2013Saturday");
        // 2h * 25.00 * 160 * 0.067 / 100 = 5.36
        assertThat(result.standbyLines().getFirst().amount()).isEqualByComparingTo(new BigDecimal("5.36"));
        assertThat(result.incidentLines()).isEmpty();
        assertThat(result.grandTotal()).isEqualByComparingTo(new BigDecimal("5.36"));
    }

    @Test
    @DisplayName("should apply sunday holiday percentage for sunday entries")
    void shouldApplySundayHolidayRateForSundayEntries() {
        // given
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(contextLoader.load(PERIOD_ID)).thenReturn(context(profile()));
        stubOvertimeBaseRate();
        // standby sunday: 4h * 25.00 * 160 * 0.084 / 100 = 13.44
        OnCallDayEntryResponse sundayEntry = new OnCallDayEntryResponse(
                LocalDate.of(2025, 4, 20), "Sunday", new BigDecimal("4"), StandbyRateType.SUNDAY_HOLIDAY, false);
        when(calculateOnCallDayEntries.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of(sundayEntry)));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of());

        // when
        EarningsResponse result = useCase.execute(new CalculateEarningsRequest(PERIOD_ID));

        // then
        assertThat(result.standbyLines().getFirst().compensationLabel()).isEqualTo("On-call Sunday / Holiday");
        assertThat(result.standbyLines().getFirst().amount()).isEqualByComparingTo(new BigDecimal("13.44"));
        assertThat(result.grandTotal()).isEqualByComparingTo(new BigDecimal("13.44"));
    }

    @Test
    @DisplayName("should calculate incident earnings with base and allowance entries")
    void shouldCalculateIncidentEarningsWithBaseAndAllowanceEntries() {
        // given
        Incident incident = new Incident(
                10L,
                PERIOD_ID,
                "Prod alert",
                LocalDateTime.of(2025, 4, 15, 22, 0),
                LocalDateTime.of(2025, 4, 15, 23, 0),
                LocalDateTime.now());

        // base: 1h * 25 * 100 / 100 = 25.00
        OvertimeEntryResponse baseEntry = new OvertimeEntryResponse(
                10L,
                new BigDecimal("1"),
                null,
                null,
                LocalDate.of(2025, 4, 15),
                LocalTime.of(22, 0),
                LocalTime.of(23, 0),
                false);
        // allowance 50%: 1h * 25 * 50 / 100 = 12.50
        OvertimeEntryResponse allowanceEntry = new OvertimeEntryResponse(
                10L,
                null,
                new BigDecimal("1"),
                new BigDecimal("50"),
                LocalDate.of(2025, 4, 15),
                LocalTime.of(22, 0),
                LocalTime.of(23, 0),
                true);

        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(contextLoader.load(PERIOD_ID)).thenReturn(context(profile()));
        stubOvertimeBaseRate();
        when(calculateOnCallDayEntries.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(incident));
        when(overtimeEntriesCalculator.calculate(any(Incident.class), any(OvertimeCalculationContext.class)))
                .thenReturn(new OvertimeEntriesResponse(10L, List.of(baseEntry, allowanceEntry)));

        // when
        EarningsResponse result = useCase.execute(new CalculateEarningsRequest(PERIOD_ID));

        // then
        assertThat(result.incidentLines()).hasSize(1);
        assertThat(result.incidentLines().getFirst().incidentId()).isEqualTo(10L);
        assertThat(result.incidentLines().getFirst().incidentName()).isEqualTo("Prod alert");
        assertThat(result.incidentLines().getFirst().subtotal()).isEqualByComparingTo(new BigDecimal("37.50"));
        assertThat(result.grandTotal()).isEqualByComparingTo(new BigDecimal("37.50"));
    }

    @Test
    @DisplayName("should build hours summary string from base and allowance entries")
    void shouldBuildHoursSummaryFromBaseAndAllowanceEntries() {
        // given
        Incident incident = new Incident(
                10L,
                PERIOD_ID,
                "Alert",
                LocalDateTime.of(2025, 4, 15, 20, 0),
                LocalDateTime.of(2025, 4, 16, 1, 0),
                LocalDateTime.now());

        OvertimeEntryResponse baseEntry = new OvertimeEntryResponse(
                10L,
                new BigDecimal("3"),
                null,
                null,
                LocalDate.of(2025, 4, 15),
                LocalTime.of(20, 0),
                LocalTime.of(23, 0),
                false);
        OvertimeEntryResponse allowance50 = new OvertimeEntryResponse(
                10L,
                null,
                new BigDecimal("2"),
                new BigDecimal("50"),
                LocalDate.of(2025, 4, 15),
                LocalTime.of(23, 0),
                LocalTime.of(1, 0),
                true);
        OvertimeEntryResponse allowance35 = new OvertimeEntryResponse(
                10L,
                null,
                new BigDecimal("1"),
                new BigDecimal("35"),
                LocalDate.of(2025, 4, 15),
                LocalTime.of(1, 0),
                LocalTime.of(2, 0),
                true);

        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(contextLoader.load(PERIOD_ID)).thenReturn(context(profile()));
        stubOvertimeBaseRate();
        when(calculateOnCallDayEntries.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(incident));
        when(overtimeEntriesCalculator.calculate(any(Incident.class), any(OvertimeCalculationContext.class)))
                .thenReturn(new OvertimeEntriesResponse(10L, List.of(baseEntry, allowance50, allowance35)));

        // when
        EarningsResponse result = useCase.execute(new CalculateEarningsRequest(PERIOD_ID));

        // then
        assertThat(result.incidentLines().getFirst().hoursSummary())
                .isEqualTo("3h overtime + 2h 50% allowance + 1h 35% allowance");
    }

    @Test
    @DisplayName("should skip incident during working hours and not include it in earnings")
    void shouldSkipIncidentDuringWorkingHours() {
        // given
        Incident incident = new Incident(
                30L,
                PERIOD_ID,
                "Working hours call",
                LocalDateTime.of(2025, 4, 15, 10, 0),
                LocalDateTime.of(2025, 4, 15, 11, 0),
                LocalDateTime.now());

        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(contextLoader.load(PERIOD_ID)).thenReturn(context(profile()));
        stubOvertimeBaseRate();
        when(calculateOnCallDayEntries.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of()));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(incident));
        when(overtimeEntriesCalculator.calculate(any(Incident.class), any(OvertimeCalculationContext.class)))
                .thenThrow(new IncidentDuringWorkingHoursException());

        // when
        EarningsResponse result = useCase.execute(new CalculateEarningsRequest(PERIOD_ID));

        // then
        assertThat(result.incidentLines()).isEmpty();
        assertThat(result.grandTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("should sum standby and incident earnings into grand total")
    void shouldSumStandbyAndIncidentEarningsIntoGrandTotal() {
        // given
        // standby weekday: 2h * 25.00 * 160 * 0.067 / 100 = 5.36
        OnCallDayEntryResponse dayEntry = new OnCallDayEntryResponse(
                LocalDate.of(2025, 4, 14), "Monday", new BigDecimal("2"), StandbyRateType.WEEKDAY_SATURDAY, false);
        Incident incident = new Incident(
                10L,
                PERIOD_ID,
                "Alert",
                LocalDateTime.of(2025, 4, 15, 22, 0),
                LocalDateTime.of(2025, 4, 15, 23, 0),
                LocalDateTime.now());
        // incident base: 1h * 25 * 100 / 100 = 25.00
        OvertimeEntryResponse baseEntry = new OvertimeEntryResponse(
                10L,
                new BigDecimal("1"),
                null,
                null,
                LocalDate.of(2025, 4, 15),
                LocalTime.of(22, 0),
                LocalTime.of(23, 0),
                false);

        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(contextLoader.load(PERIOD_ID)).thenReturn(context(profile()));
        stubOvertimeBaseRate();
        when(calculateOnCallDayEntries.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of(dayEntry)));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(incident));
        when(overtimeEntriesCalculator.calculate(any(Incident.class), any(OvertimeCalculationContext.class)))
                .thenReturn(new OvertimeEntriesResponse(10L, List.of(baseEntry)));

        // when
        EarningsResponse result = useCase.execute(new CalculateEarningsRequest(PERIOD_ID));

        // then
        // 5.36 + 25.00 = 30.36
        assertThat(result.grandTotal()).isEqualByComparingTo(new BigDecimal("30.36"));
    }

    @Test
    @DisplayName("should throw exception when period is not found")
    void shouldThrowExceptionWhenPeriodIsNotFound() {
        // given
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.empty());

        // when / then
        var request = new CalculateEarningsRequest(PERIOD_ID);
        assertThatExceptionOfType(InvalidOnCallPeriodException.class).isThrownBy(() -> useCase.execute(request));
    }

    @Test
    @DisplayName("should throw ProfileNotFoundException before calculating earnings when profile is absent")
    void shouldThrowProfileNotFoundExceptionBeforeCalculatingEarningsWhenProfileIsAbsent() {
        // given
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(contextLoader.load(PERIOD_ID)).thenThrow(new ProfileNotFoundException("EngineerProfile not found"));

        // when / then
        var request = new CalculateEarningsRequest(PERIOD_ID);
        assertThatExceptionOfType(ProfileNotFoundException.class)
                .isThrownBy(() -> useCase.execute(request))
                .withMessage("EngineerProfile not found");
        verifyNoInteractions(
                compensationRateGateway, calculateOnCallDayEntries, incidentGateway, overtimeEntriesCalculator);
    }

    @Test
    @DisplayName("should throw exception when overtime base compensation rate is not configured")
    void shouldThrowExceptionWhenCompensationRateNotConfigured() {
        // given
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(contextLoader.load(PERIOD_ID)).thenReturn(context(profile()));
        when(compensationRateGateway.findByRateCategory(RateCategory.OVERTIME_BASE))
                .thenReturn(List.of());

        // when / then
        var request = new CalculateEarningsRequest(PERIOD_ID);
        assertThatExceptionOfType(CompensationRateNotFoundException.class).isThrownBy(() -> useCase.execute(request));
    }

    @Test
    @DisplayName("should calculate standby amount with the minimum profile percentage")
    void shouldCalculateStandbyAmountWithMinimumProfilePercentage() {
        // given
        EngineerProfile profileWithMinimumPercentage = new EngineerProfile(
                1L,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Money.of(HOURLY_RATE),
                Percentage.of(new BigDecimal("0.001")),
                Percentage.of(new BigDecimal("0.001")),
                LocalDateTime.now());
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(contextLoader.load(PERIOD_ID)).thenReturn(context(profileWithMinimumPercentage));
        stubOvertimeBaseRate();
        OnCallDayEntryResponse dayEntry = new OnCallDayEntryResponse(
                LocalDate.of(2025, 4, 14), "Monday", new BigDecimal("8"), StandbyRateType.WEEKDAY_SATURDAY, false);
        when(calculateOnCallDayEntries.calculate(any(), any(), any()))
                .thenReturn(new OnCallDayEntriesResponse(PERIOD_ID, List.of(dayEntry)));
        when(incidentGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of());

        // when
        EarningsResponse result = useCase.execute(new CalculateEarningsRequest(PERIOD_ID));

        // then
        assertThat(result.standbyLines().getFirst().amount()).isEqualByComparingTo(new BigDecimal("0.32"));
        assertThat(result.grandTotal()).isEqualByComparingTo(new BigDecimal("0.32"));
    }
}
