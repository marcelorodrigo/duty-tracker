package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.CalculateOvertimeEntriesValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CalculateOvertimeEntriesUseCaseTest {

    @Mock
    IncidentGateway incidentGateway;

    @Mock
    EngineerProfileGateway engineerProfileGateway;

    @Mock
    CompensationRateGateway compensationRateGateway;

    @Mock
    HolidayGateway holidayGateway;

    @Mock
    CalculateOvertimeEntriesValidator validator;

    CalculateOvertimeEntriesUseCase useCase;

    private static final EngineerProfile PROFILE = new EngineerProfile(
            1L,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            BigDecimal.valueOf(50.00),
            new BigDecimal("0.067"),
            new BigDecimal("0.084"),
            LocalDateTime.now());

    private static final EngineerProfile PROFILE_WITH_SATURDAY = new EngineerProfile(
            2L,
            Set.of(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            BigDecimal.valueOf(50.00),
            new BigDecimal("0.067"),
            new BigDecimal("0.084"),
            LocalDateTime.now());

    private static final EngineerProfile PROFILE_WITH_SUNDAY = new EngineerProfile(
            3L,
            Set.of(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SUNDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            BigDecimal.valueOf(50.00),
            new BigDecimal("0.067"),
            new BigDecimal("0.084"),
            LocalDateTime.now());

    @BeforeEach
    void setUp() {
        useCase = new CalculateOvertimeEntriesUseCase(
                incidentGateway, engineerProfileGateway, compensationRateGateway, holidayGateway, validator);
    }

    private void givenNoHolidays(Long periodId) {
        when(holidayGateway.findByOnCallPeriodId(periodId)).thenReturn(List.of());
    }

    private void givenNoAllowanceRates(OvertimeDayType dayType) {
        when(compensationRateGateway.findByRateCategoryAndOvertimeDayType(RateCategory.OVERTIME_ALLOWANCE, dayType))
                .thenReturn(List.of());
    }

    private static BigDecimal hours(int h) {
        return BigDecimal.valueOf(h).setScale(4, RoundingMode.UNNECESSARY);
    }

    // ── Test 1 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should create base overtime entry when incident is entirely before working hours")
    void shouldCreateBaseOvertimeEntryWhenIncidentIsEntirelyBeforeWorkingHours() {
        // given — Tuesday Apr 14 2026, 02:00–03:45 (105 min → ceil=2h)
        LocalDate date = LocalDate.of(2026, 4, 14); // Tuesday
        Incident incident = new Incident(
                10L,
                1L,
                "Test incident",
                LocalDateTime.of(date, LocalTime.of(2, 0)),
                LocalDateTime.of(date, LocalTime.of(3, 45)),
                LocalDateTime.now());

        when(incidentGateway.findById(10L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays(1L);
        givenNoAllowanceRates(OvertimeDayType.WEEKDAY);

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(10L));

        // then
        assertThat(result.incidentId()).isEqualTo(10L);
        assertThat(result.entries()).hasSize(1);

        OvertimeEntryResponse entry = result.entries().getFirst();
        assertThat(entry.isAllowanceEntry()).isFalse();
        assertThat(entry.overtimeHours()).isEqualByComparingTo(hours(2));
        assertThat(entry.timeFrom()).isEqualTo(LocalTime.of(2, 0));
        assertThat(entry.timeTo()).isEqualTo(LocalTime.of(3, 45));
    }

    // ── Test 2 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should throw IncidentDuringWorkingHoursException when all hours fall within working hours")
    void shouldThrowIncidentDuringWorkingHoursExceptionWhenAllHoursFallWithinWorkingHours() {
        // given — Tuesday Apr 14 2026, 10:00–11:30 (entirely inside 09:00–17:00)
        LocalDate date = LocalDate.of(2026, 4, 14);
        Incident incident = new Incident(
                20L,
                1L,
                "Test incident",
                LocalDateTime.of(date, LocalTime.of(10, 0)),
                LocalDateTime.of(date, LocalTime.of(11, 30)),
                LocalDateTime.now());

        when(incidentGateway.findById(20L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays(1L);

        // when / then
        var request = new CalculateOvertimeEntriesRequest(20L);
        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(IncidentDuringWorkingHoursException.class);
    }

    // ── Test 3 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should treat full incident as overtime when incident falls on Sunday")
    void shouldTreatFullIncidentAsOvertimeWhenIncidentFallsOnSunday() {
        // given — Sunday Apr 19 2026, 10:00–11:00 (60 min → 1h)
        LocalDate sunday = LocalDate.of(2026, 4, 19); // Sunday
        assertThat(sunday.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);

        Incident incident = new Incident(
                50L,
                1L,
                "Sunday incident",
                LocalDateTime.of(sunday, LocalTime.of(10, 0)),
                LocalDateTime.of(sunday, LocalTime.of(11, 0)),
                LocalDateTime.now());

        when(incidentGateway.findById(50L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays(1L);
        givenNoAllowanceRates(OvertimeDayType.SUNDAY_HOLIDAY);

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(50L));

        // then — full segment 10:00–11:00, 60 min → 1h
        assertThat(result.entries()).hasSize(1);
        OvertimeEntryResponse entry = result.entries().getFirst();
        assertThat(entry.isAllowanceEntry()).isFalse();
        assertThat(entry.overtimeHours()).isEqualByComparingTo(hours(1));
        assertThat(entry.timeFrom()).isEqualTo(LocalTime.of(10, 0));
        assertThat(entry.timeTo()).isEqualTo(LocalTime.of(11, 0));
    }

    // ── Test 4 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should treat Sunday as full overtime even if configured as working day")
    void shouldTreatSundayAsFullOvertimeEvenIfConfiguredAsWorkingDay() {
        // given — Sunday Apr 19 2026, 10:00–11:00 with profile that includes Sunday
        LocalDate sunday = LocalDate.of(2026, 4, 19); // Sunday
        assertThat(sunday.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);

        Incident incident = new Incident(
                51L,
                1L,
                "Sunday incident with Sunday in working days",
                LocalDateTime.of(sunday, LocalTime.of(10, 0)),
                LocalDateTime.of(sunday, LocalTime.of(11, 0)),
                LocalDateTime.now());

        when(incidentGateway.findById(51L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE_WITH_SUNDAY));
        givenNoHolidays(1L);
        givenNoAllowanceRates(OvertimeDayType.SUNDAY_HOLIDAY);

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(51L));

        // then — full segment 10:00–11:00, 60 min → 1h
        assertThat(result.entries()).hasSize(1);
        OvertimeEntryResponse entry = result.entries().getFirst();
        assertThat(entry.isAllowanceEntry()).isFalse();
        assertThat(entry.overtimeHours()).isEqualByComparingTo(hours(1));
        assertThat(entry.timeFrom()).isEqualTo(LocalTime.of(10, 0));
        assertThat(entry.timeTo()).isEqualTo(LocalTime.of(11, 0));
    }

    // ── Test 5 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should create base and allowance entries when a matching OVERTIME_ALLOWANCE rate zone exists")
    void shouldCreateBaseAndAllowanceEntriesWhenMatchingRateZoneExists() {
        // given — Tuesday Apr 14 2026, 22:00–23:30 (90 min → ceil=2h), rate zone 22:00–23:59 at 50%
        LocalDate date = LocalDate.of(2026, 4, 14); // Tuesday
        Incident incident = new Incident(
                60L,
                1L,
                "Evening incident",
                LocalDateTime.of(date, LocalTime.of(22, 0)),
                LocalDateTime.of(date, LocalTime.of(23, 30)),
                LocalDateTime.now());

        CompensationRate allowanceRate = new CompensationRate(
                1L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Evening allowance",
                LocalTime.of(22, 0),
                LocalTime.of(23, 59),
                new BigDecimal("50.00"));

        when(incidentGateway.findById(60L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays(1L);
        when(compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                        RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.WEEKDAY))
                .thenReturn(List.of(allowanceRate));

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(60L));

        // then — 1 base + 1 allowance entry
        assertThat(result.entries()).hasSize(2);

        OvertimeEntryResponse base = result.entries().stream()
                .filter(e -> !e.isAllowanceEntry())
                .findFirst()
                .orElseThrow();
        assertThat(base.overtimeHours()).isEqualByComparingTo(hours(2));
        assertThat(base.timeFrom()).isEqualTo(LocalTime.of(22, 0));
        assertThat(base.timeTo()).isEqualTo(LocalTime.of(23, 30));

        OvertimeEntryResponse allowance = result.entries().stream()
                .filter(OvertimeEntryResponse::isAllowanceEntry)
                .findFirst()
                .orElseThrow();
        assertThat(allowance.allowanceHours()).isEqualByComparingTo(hours(2));
        assertThat(allowance.allowancePercentage()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(allowance.timeFrom()).isEqualTo(LocalTime.of(22, 0));
        assertThat(allowance.timeTo()).isEqualTo(LocalTime.of(23, 30));
    }

    // ── Test 5 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should throw InvalidIncidentException when incident is not found")
    void shouldThrowInvalidIncidentExceptionWhenIncidentIsNotFound() {
        // given
        when(incidentGateway.findById(99L)).thenReturn(Optional.empty());

        // when / then
        var request = new CalculateOvertimeEntriesRequest(99L);
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("99");
    }

    // ── Test 6 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should apply SATURDAY allowance rates when incident falls on a Saturday")
    void shouldApplySaturdayAllowanceRatesWhenIncidentFallsOnSaturday() {
        // given — Saturday Apr 18 2026, 22:00–23:30 (90 min → ceil=2h), rate zone 22:00–00:00 at 75%
        LocalDate saturday = LocalDate.of(2026, 4, 18);
        assertThat(saturday.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);

        Incident incident = new Incident(
                70L,
                1L,
                "Saturday incident",
                LocalDateTime.of(saturday, LocalTime.of(22, 0)),
                LocalDateTime.of(saturday, LocalTime.of(23, 30)),
                LocalDateTime.now());

        CompensationRate saturdayNightRate = new CompensationRate(
                2L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.SATURDAY,
                "Saturday night",
                LocalTime.of(22, 0),
                LocalTime.MIDNIGHT,
                new BigDecimal("75.00"));

        when(incidentGateway.findById(70L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays(1L);
        when(compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                        RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.SATURDAY))
                .thenReturn(List.of(saturdayNightRate));

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(70L));

        // then — SATURDAY rates applied: 1 base + 1 allowance at 75%
        assertThat(result.entries()).hasSize(2);

        OvertimeEntryResponse base = result.entries().stream()
                .filter(e -> !e.isAllowanceEntry())
                .findFirst()
                .orElseThrow();
        assertThat(base.overtimeHours()).isEqualByComparingTo(hours(2));

        OvertimeEntryResponse allowance = result.entries().stream()
                .filter(OvertimeEntryResponse::isAllowanceEntry)
                .findFirst()
                .orElseThrow();
        assertThat(allowance.allowanceHours()).isEqualByComparingTo(hours(2));
        assertThat(allowance.allowancePercentage()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    // ── Test 7 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should treat Saturday incident during working hours as full overtime")
    void shouldTreatSaturdayIncidentDuringWorkingHoursAsFullOvertime() {
        // given — Saturday Apr 18 2026, 10:00–11:00 (60 min → 1h), rate zone 10:00–11:00 at 50%
        LocalDate saturday = LocalDate.of(2026, 4, 18);
        assertThat(saturday.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);

        Incident incident = new Incident(
                71L,
                1L,
                "Saturday working-hours incident",
                LocalDateTime.of(saturday, LocalTime.of(10, 0)),
                LocalDateTime.of(saturday, LocalTime.of(11, 0)),
                LocalDateTime.now());

        CompensationRate saturdayDayRate = new CompensationRate(
                3L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.SATURDAY,
                "Saturday day",
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                new BigDecimal("50.00"));

        when(incidentGateway.findById(71L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays(1L);
        when(compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                        RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.SATURDAY))
                .thenReturn(List.of(saturdayDayRate));

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(71L));

        // then — full segment 10:00–11:00, 1 base + 1 allowance at 50%
        assertThat(result.entries()).hasSize(2);

        OvertimeEntryResponse base = result.entries().stream()
                .filter(e -> !e.isAllowanceEntry())
                .findFirst()
                .orElseThrow();
        assertThat(base.overtimeHours()).isEqualByComparingTo(hours(1));
        assertThat(base.timeFrom()).isEqualTo(LocalTime.of(10, 0));
        assertThat(base.timeTo()).isEqualTo(LocalTime.of(11, 0));

        OvertimeEntryResponse allowance = result.entries().stream()
                .filter(OvertimeEntryResponse::isAllowanceEntry)
                .findFirst()
                .orElseThrow();
        assertThat(allowance.allowanceHours()).isEqualByComparingTo(hours(1));
        assertThat(allowance.allowancePercentage()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    // ── Test 8 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should treat full Saturday incident spanning working hours as overtime")
    void shouldTreatFullSaturdayIncidentSpanningWorkingHoursAsOvertime() {
        // given — Saturday Apr 18 2026, 08:30–14:40 (370 min → ceil=7h)
        LocalDate saturday = LocalDate.of(2026, 4, 18);
        assertThat(saturday.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);

        Incident incident = new Incident(
                72L,
                1L,
                "Saturday long incident",
                LocalDateTime.of(saturday, LocalTime.of(8, 30)),
                LocalDateTime.of(saturday, LocalTime.of(14, 40)),
                LocalDateTime.now());

        CompensationRate saturdayDayRate = new CompensationRate(
                4L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.SATURDAY,
                "Saturday full day",
                LocalTime.of(8, 0),
                LocalTime.of(15, 0),
                new BigDecimal("50.00"));

        when(incidentGateway.findById(72L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays(1L);
        when(compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                        RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.SATURDAY))
                .thenReturn(List.of(saturdayDayRate));

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(72L));

        // then — full segment 08:30–14:40, 7h base + 7h allowance at 50%
        assertThat(result.entries()).hasSize(2);

        OvertimeEntryResponse base = result.entries().stream()
                .filter(e -> !e.isAllowanceEntry())
                .findFirst()
                .orElseThrow();
        assertThat(base.overtimeHours()).isEqualByComparingTo(hours(7));
        assertThat(base.timeFrom()).isEqualTo(LocalTime.of(8, 30));
        assertThat(base.timeTo()).isEqualTo(LocalTime.of(14, 40));

        OvertimeEntryResponse allowance = result.entries().stream()
                .filter(OvertimeEntryResponse::isAllowanceEntry)
                .findFirst()
                .orElseThrow();
        assertThat(allowance.allowanceHours()).isEqualByComparingTo(hours(7));
        assertThat(allowance.allowancePercentage()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    // ── Test 9 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should apply working-hours filter when Saturday is configured as working day")
    void shouldApplyWorkingHoursFilterWhenSaturdayIsConfiguredAsWorkingDay() {
        // given — Saturday Apr 18 2026, 10:00–11:00 with profile that includes Saturday
        LocalDate saturday = LocalDate.of(2026, 4, 18);
        assertThat(saturday.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);

        Incident incident = new Incident(
                73L,
                1L,
                "Saturday working-hours incident",
                LocalDateTime.of(saturday, LocalTime.of(10, 0)),
                LocalDateTime.of(saturday, LocalTime.of(11, 0)),
                LocalDateTime.now());

        when(incidentGateway.findById(73L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE_WITH_SATURDAY));
        givenNoHolidays(1L);

        // when / then
        var request = new CalculateOvertimeEntriesRequest(73L);
        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(IncidentDuringWorkingHoursException.class);
    }

    // ── Test 10 ─────────────────────────────────────────────────────────────-

    @Test
    @DisplayName("should default to Mon-Fri working days when profile is absent")
    void shouldDefaultToMonFriWorkingDaysWhenProfileIsAbsent() {
        // given — Saturday Apr 18 2026, 10:00–11:00, no profile exists
        LocalDate saturday = LocalDate.of(2026, 4, 18);
        assertThat(saturday.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);

        Incident incident = new Incident(
                74L,
                1L,
                "Saturday incident without profile",
                LocalDateTime.of(saturday, LocalTime.of(10, 0)),
                LocalDateTime.of(saturday, LocalTime.of(11, 0)),
                LocalDateTime.now());

        CompensationRate saturdayDayRate = new CompensationRate(
                5L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.SATURDAY,
                "Saturday day",
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                new BigDecimal("50.00"));

        when(incidentGateway.findById(74L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.empty());
        givenNoHolidays(1L);
        when(compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                        RateCategory.OVERTIME_ALLOWANCE, OvertimeDayType.SATURDAY))
                .thenReturn(List.of(saturdayDayRate));

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(74L));

        // then — full segment 10:00–11:00, 1 base + 1 allowance at 50%
        assertThat(result.entries()).hasSize(2);

        OvertimeEntryResponse base = result.entries().stream()
                .filter(e -> !e.isAllowanceEntry())
                .findFirst()
                .orElseThrow();
        assertThat(base.overtimeHours()).isEqualByComparingTo(hours(1));

        OvertimeEntryResponse allowance = result.entries().stream()
                .filter(OvertimeEntryResponse::isAllowanceEntry)
                .findFirst()
                .orElseThrow();
        assertThat(allowance.allowanceHours()).isEqualByComparingTo(hours(1));
        assertThat(allowance.allowancePercentage()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    // ── Test 11 ─────────────────────────────────────────────────────────────-

    @Test
    @DisplayName("should treat holiday date as SUNDAY_HOLIDAY for overtime calculation")
    void shouldTreatHolidayDateAsSundayHoliday() {
        // given — Monday Apr 14 2026 with holiday, 10:00–11:00 → full segment (1h)
        LocalDate date = LocalDate.of(2026, 4, 14); // Monday but overridden as holiday
        Incident incident = new Incident(
                80L,
                1L,
                "Holiday incident",
                LocalDateTime.of(date, LocalTime.of(10, 0)),
                LocalDateTime.of(date, LocalTime.of(11, 0)),
                LocalDateTime.now());

        when(incidentGateway.findById(80L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(holidayGateway.findByOnCallPeriodId(1L)).thenReturn(List.of(new Holiday(1L, 1L, date, "Liberation Day")));
        givenNoAllowanceRates(OvertimeDayType.SUNDAY_HOLIDAY);

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(80L));

        // then — full incident treated as overtime (not just outside working hours)
        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().getFirst().overtimeHours()).isEqualByComparingTo(hours(1));
    }

    // ── Test 8 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should assign correct dates when overnight incident crosses midnight")
    void shouldAssignCorrectDatesWhenOvernightIncidentCrossesMidnight() {
        // given — Monday May 4 2026, 23:00 → Tuesday May 5 2026, 00:45 (overnight)
        // Expected segments (weekday, work hours 09:00-17:00):
        //   - 23:00–00:00 on May 4  (after work hours)
        //   - 00:00–00:45 on May 5  (before work hours)
        LocalDate may4 = LocalDate.of(2026, 5, 4); // Monday
        LocalDate may5 = LocalDate.of(2026, 5, 5); // Tuesday
        Incident incident = new Incident(
                90L,
                1L,
                "Memory Leak Investigation",
                LocalDateTime.of(may4, LocalTime.of(23, 0)),
                LocalDateTime.of(may5, LocalTime.of(0, 45)),
                LocalDateTime.now());

        when(incidentGateway.findById(90L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays(1L);
        givenNoAllowanceRates(OvertimeDayType.WEEKDAY);

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(90L));

        // then — two base entries (no allowance rates set), each with the correct date
        List<OvertimeEntryResponse> entries = result.entries();
        assertThat(entries).hasSize(2);

        OvertimeEntryResponse beforeMidnight = entries.stream()
                .filter(e -> !e.isAllowanceEntry() && e.timeFrom().equals(LocalTime.of(23, 0)))
                .findFirst()
                .orElseThrow();
        assertThat(beforeMidnight.date()).isEqualTo(may4);
        assertThat(beforeMidnight.timeTo()).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(beforeMidnight.overtimeHours()).isEqualByComparingTo(hours(1));

        OvertimeEntryResponse afterMidnight = entries.stream()
                .filter(e -> !e.isAllowanceEntry() && e.timeFrom().equals(LocalTime.MIDNIGHT))
                .findFirst()
                .orElseThrow();
        assertThat(afterMidnight.date()).isEqualTo(may5);
        assertThat(afterMidnight.timeTo()).isEqualTo(LocalTime.of(0, 45));
        assertThat(afterMidnight.overtimeHours()).isEqualByComparingTo(hours(1));
    }

    // NOTE: zero-duration incidents are validated earlier by the request validator; no defensive check here.
}
