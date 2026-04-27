package com.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.dutytracker.domain.exceptions.InvalidIncidentException;
import com.dutytracker.domain.exceptions.OvertimeDayOffException;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.gateway.holiday.PublicHolidayGateway;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.gateway.incident.OvertimeEntryGateway;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.response.incident.*;
import com.dutytracker.usecase.validator.incident.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
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

@ExtendWith(MockitoExtension.class)
class CalculateOvertimeEntriesUseCaseTest {

    @Mock
    IncidentGateway incidentGateway;

    @Mock
    EngineerProfileGateway engineerProfileGateway;

    @Mock
    CompensationRateGateway compensationRateGateway;

    @Mock
    OvertimeEntryGateway overtimeEntryGateway;

    @Mock
    OnCallDayEntryGateway onCallDayEntryGateway;

    @Mock
    PublicHolidayGateway publicHolidayGateway;

    @Mock
    CalculateOvertimeEntriesValidator validator;

    CalculateOvertimeEntriesUseCase useCase;

    private static final EngineerProfile PROFILE = new EngineerProfile(
            1L,
            EmployeeType.INTERNAL,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            Instant.now());

    @BeforeEach
    void setUp() {
        useCase = new CalculateOvertimeEntriesUseCase(
                incidentGateway,
                engineerProfileGateway,
                compensationRateGateway,
                overtimeEntryGateway,
                onCallDayEntryGateway,
                publicHolidayGateway,
                validator);
    }

    /** Stubs saveAll to return entries with sequential IDs. */
    private void givenSaveAllReturnsEntries() {
        when(overtimeEntryGateway.saveAll(anyList())).thenAnswer(inv -> {
            List<OvertimeEntry> input = inv.getArgument(0);
            long id = 1L;
            List<OvertimeEntry> result = new java.util.ArrayList<>();
            for (OvertimeEntry e : input) {
                result.add(new OvertimeEntry(
                        id++,
                        e.incidentId(),
                        e.overtimeHours(),
                        e.allowanceHours(),
                        e.allowancePercentage(),
                        e.timeFrom(),
                        e.timeTo(),
                        e.isAllowanceEntry(),
                        e.manualOverride()));
            }
            return result;
        });
    }

    private void givenNoExistingOvertimeEntries() {
        when(overtimeEntryGateway.findByIncidentId(anyLong())).thenReturn(List.of());
    }

    private static BigDecimal hours(int h) {
        return BigDecimal.valueOf(h).setScale(4, RoundingMode.UNNECESSARY);
    }

    // ── Test 1 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should create base overtime entry when incident is entirely before working hours")
    void shouldCreateBaseOvertimeEntryWhenIncidentIsEntirelyBeforeWorkingHours() {
        // given — Tuesday Apr 15, 02:00–03:45 (105 min → ceil=2h)
        LocalDate date = LocalDate.of(2026, 4, 14); // Tuesday
        Incident incident = new Incident(10L, null, date, LocalTime.of(2, 0), LocalTime.of(3, 45), Instant.now());

        when(incidentGateway.findById(10L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(publicHolidayGateway.isHoliday(date)).thenReturn(false);
        when(compensationRateGateway.findByEmployeeType(EmployeeType.INTERNAL)).thenReturn(List.of());
        givenNoExistingOvertimeEntries();
        givenSaveAllReturnsEntries();

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
        assertThat(entry.manualOverride()).isFalse();
    }

    // ── Test 2 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should throw IncidentDuringWorkingHoursException when all hours fall within working hours")
    void shouldThrowIncidentDuringWorkingHoursExceptionWhenAllHoursFallWithinWorkingHours() {
        // given — Tuesday Apr 15, 10:00–11:30 (entirely inside 09:00–17:00)
        LocalDate date = LocalDate.of(2026, 4, 14);
        Incident incident = new Incident(20L, null, date, LocalTime.of(10, 0), LocalTime.of(11, 30), Instant.now());

        when(incidentGateway.findById(20L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(publicHolidayGateway.isHoliday(date)).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> useCase.execute(new CalculateOvertimeEntriesRequest(20L)))
                .isInstanceOf(IncidentDuringWorkingHoursException.class);

        verify(overtimeEntryGateway, never()).saveAll(any());
    }

    // ── Test 3 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should throw OvertimeDayOffException when on-call day entry has timeForTimeFlag set")
    void shouldThrowOvertimeDayOffExceptionWhenDayEntryHasTimeForTimeFlag() {
        // given — Monday Apr 14, onCallPeriodId=1, day entry has timeForTimeFlag=true
        LocalDate date = LocalDate.of(2026, 4, 13); // Monday
        Incident incident = new Incident(30L, 1L, date, LocalTime.of(2, 0), LocalTime.of(3, 0), Instant.now());

        OnCallDayEntry dayEntry = new OnCallDayEntry(
                100L, 1L, date, BigDecimal.valueOf(24), StandbyRateType.WEEKDAY_SATURDAY, false, true, false);

        when(incidentGateway.findById(30L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(onCallDayEntryGateway.findByOnCallPeriodId(1L)).thenReturn(List.of(dayEntry));

        // when / then
        assertThatThrownBy(() -> useCase.execute(new CalculateOvertimeEntriesRequest(30L)))
                .isInstanceOf(OvertimeDayOffException.class)
                .hasMessageContaining("Time-for-time");

        verify(overtimeEntryGateway, never()).saveAll(any());
    }

    // ── Test 4 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should skip day-off check and create entries when incident has no onCallPeriodId")
    void shouldSkipDayOffCheckAndCreateEntriesWhenIncidentHasNoOnCallPeriodId() {
        // given — non-on-call incident (onCallPeriodId=null): no day-off check performed
        LocalDate date = LocalDate.of(2026, 4, 13); // Monday
        Incident incident = new Incident(40L, null, date, LocalTime.of(6, 0), LocalTime.of(7, 0), Instant.now());

        when(incidentGateway.findById(40L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(publicHolidayGateway.isHoliday(date)).thenReturn(false);
        when(compensationRateGateway.findByEmployeeType(EmployeeType.INTERNAL)).thenReturn(List.of());
        givenNoExistingOvertimeEntries();
        givenSaveAllReturnsEntries();

        // when
        OvertimeEntriesResponse result = useCase.execute(new CalculateOvertimeEntriesRequest(40L));

        // then — day-off gateway never called, entries are created
        verify(onCallDayEntryGateway, never()).findByOnCallPeriodId(anyLong());
        assertThat(result.entries()).isNotEmpty();
    }

    // ── Test 5 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should treat full incident as overtime when incident falls on Sunday")
    void shouldTreatFullIncidentAsOvertimeWhenIncidentFallsOnSunday() {
        // given — Sunday Apr 19, 10:00–11:00 (60 min → 1h)
        LocalDate sunday = LocalDate.of(2026, 4, 19); // Sunday
        assertThat(sunday.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);

        Incident incident = new Incident(50L, null, sunday, LocalTime.of(10, 0), LocalTime.of(11, 0), Instant.now());

        when(incidentGateway.findById(50L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(publicHolidayGateway.isHoliday(sunday)).thenReturn(false); // DayOfWeek=SUNDAY triggers holiday path
        when(compensationRateGateway.findByEmployeeType(EmployeeType.INTERNAL)).thenReturn(List.of());
        givenNoExistingOvertimeEntries();
        givenSaveAllReturnsEntries();

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

    // ── Test 6 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should create base and allowance entries when a matching OVERTIME_ALLOWANCE rate zone exists")
    void shouldCreateBaseAndAllowanceEntriesWhenMatchingRateZoneExists() {
        // given — Tuesday Apr 15, 22:00–23:30 (90 min → ceil=2h), rate zone 22:00–23:59 at 50%
        LocalDate date = LocalDate.of(2026, 4, 14); // Tuesday
        Incident incident = new Incident(60L, null, date, LocalTime.of(22, 0), LocalTime.of(23, 30), Instant.now());

        CompensationRate allowanceRate = new CompensationRate(
                1L,
                EmployeeType.INTERNAL,
                RateCategory.OVERTIME_ALLOWANCE,
                "Evening allowance",
                LocalTime.of(22, 0),
                LocalTime.of(23, 59),
                new BigDecimal("50.00"));

        when(incidentGateway.findById(60L)).thenReturn(Optional.of(incident));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(publicHolidayGateway.isHoliday(date)).thenReturn(false);
        when(compensationRateGateway.findByEmployeeType(EmployeeType.INTERNAL)).thenReturn(List.of(allowanceRate));
        givenNoExistingOvertimeEntries();
        givenSaveAllReturnsEntries();

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

    // ── Test 7 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should throw InvalidIncidentException when incident is not found")
    void shouldThrowInvalidIncidentExceptionWhenIncidentIsNotFound() {
        // given
        when(incidentGateway.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(new CalculateOvertimeEntriesRequest(99L)))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("99");

        verify(overtimeEntryGateway, never()).saveAll(any());
    }
}
