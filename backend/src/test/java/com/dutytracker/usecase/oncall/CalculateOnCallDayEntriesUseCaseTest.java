package com.dutytracker.usecase.oncall;










import com.dutytracker.domain.*;
import com.dutytracker.domain.StandbyRateType;
import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.holiday.PublicHolidayGateway;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.response.oncall.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CalculateOnCallDayEntriesUseCaseTest {

    @Mock OnCallPeriodGateway onCallPeriodGateway;
    @Mock HolidayOverrideGateway holidayOverrideGateway;
    @Mock EngineerProfileGateway engineerProfileGateway;
    @Mock OnCallDayEntryGateway onCallDayEntryGateway;
    @Mock PublicHolidayGateway publicHolidayGateway;
    @Mock CalculateOnCallDayEntriesValidator validator;

    CalculateOnCallDayEntriesUseCase useCase;

    // Mon–Fri working days, 09:00–17:00
    private static final EngineerProfile PROFILE = new EngineerProfile(
            1L,
            EmployeeType.INTERNAL,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            Instant.now()
    );

    @BeforeEach
    void setUp() {
        useCase = new CalculateOnCallDayEntriesUseCase(
                onCallPeriodGateway, holidayOverrideGateway, engineerProfileGateway,
                onCallDayEntryGateway, publicHolidayGateway, validator);
    }

    private void givenNoExistingEntries() {
        when(onCallDayEntryGateway.findByOnCallPeriodId(anyLong())).thenReturn(List.of());
    }

    private void givenNoHolidayOverrides() {
        when(holidayOverrideGateway.findByOnCallPeriodId(anyLong())).thenReturn(List.of());
    }

    private void givenNoPublicHolidays() {
        when(publicHolidayGateway.isHoliday(any())).thenReturn(false);
    }

    /** Saves return the same entries with sequential IDs for inspection */
    private void givenSaveAllReturnsEntries() {
        when(onCallDayEntryGateway.saveAll(anyList())).thenAnswer(inv -> {
            List<OnCallDayEntry> input = inv.getArgument(0);
            long id = 1L;
            java.util.List<OnCallDayEntry> result = new java.util.ArrayList<>();
            for (OnCallDayEntry e : input) {
                result.add(new OnCallDayEntry(id++, e.onCallPeriodId(), e.date(), e.hours(),
                        e.rateType(), e.capped(), e.timeForTimeFlag(), e.manualOverride()));
            }
            return result;
        });
    }

    private static BigDecimal hours(double h) {
        return BigDecimal.valueOf(h).setScale(4, RoundingMode.HALF_UP);
    }

    // ── Test 1 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("fullMonToMonWeekNoHolidays — Mon 14:00 to following Mon 14:00 produces 8 entries with correct hours and caps")
    void fullMonToMonWeekNoHolidays() {
        // Mon Apr 14 14:00 → Mon Apr 21 14:00  (April 2025 dates, Mon = Apr 14)
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 14, 0); // Monday
        LocalDateTime end   = LocalDateTime.of(2025, 4, 21, 14, 0); // Monday
        long periodId = 1L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, Instant.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidayOverrides();
        givenNoPublicHolidays();
        givenNoExistingEntries();
        givenSaveAllReturnsEntries();

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        assertThat(result.periodId()).isEqualTo(periodId);
        List<OnCallDayEntryResponse> entries = result.entries();
        assertThat(entries).hasSize(8);

        // Apr 14 Mon — start day: 24-14=10h, WEEKDAY_SATURDAY, not capped (10 < 15)
        assertEntry(entries.get(0), LocalDate.of(2025, 4, 14), hours(10), StandbyRateType.WEEKDAY_SATURDAY, false);

        // Apr 15 Tue — full day 24h → working day cap to 15h, capped
        assertEntry(entries.get(1), LocalDate.of(2025, 4, 15), hours(15), StandbyRateType.WEEKDAY_SATURDAY, true);

        // Apr 16 Wed — full day 24h → working day cap to 15h, capped
        assertEntry(entries.get(2), LocalDate.of(2025, 4, 16), hours(15), StandbyRateType.WEEKDAY_SATURDAY, true);

        // Apr 17 Thu
        assertEntry(entries.get(3), LocalDate.of(2025, 4, 17), hours(15), StandbyRateType.WEEKDAY_SATURDAY, true);

        // Apr 18 Fri
        assertEntry(entries.get(4), LocalDate.of(2025, 4, 18), hours(15), StandbyRateType.WEEKDAY_SATURDAY, true);

        // Apr 19 Sat — not working day, no cap
        assertEntry(entries.get(5), LocalDate.of(2025, 4, 19), hours(24), StandbyRateType.WEEKDAY_SATURDAY, false);

        // Apr 20 Sun — SUNDAY_HOLIDAY, not working day, no cap
        assertEntry(entries.get(6), LocalDate.of(2025, 4, 20), hours(24), StandbyRateType.SUNDAY_HOLIDAY, false);

        // Apr 21 Mon — end day: 14h, WEEKDAY_SATURDAY, working day but 14 < 15 → not capped
        assertEntry(entries.get(7), LocalDate.of(2025, 4, 21), hours(14), StandbyRateType.WEEKDAY_SATURDAY, false);
    }

    // ── Test 2 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("holidayOverrideChangesRateTypeToSundayHoliday — override on Mon changes it to SUNDAY_HOLIDAY")
    void holidayOverrideChangesRateTypeToSundayHoliday() {
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 8, 0); // Monday
        LocalDateTime end   = LocalDateTime.of(2025, 4, 15, 8, 0); // Tuesday
        long periodId = 2L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, Instant.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(holidayOverrideGateway.findByOnCallPeriodId(periodId))
                .thenReturn(List.of(new HolidayOverride(1L, periodId, LocalDate.of(2025, 4, 14))));
        givenNoPublicHolidays();
        givenNoExistingEntries();
        givenSaveAllReturnsEntries();

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        List<OnCallDayEntryResponse> entries = result.entries();
        assertThat(entries).hasSize(2);

        // Apr 14 Mon — holiday override → SUNDAY_HOLIDAY
        assertThat(entries.get(0).date()).isEqualTo(LocalDate.of(2025, 4, 14));
        assertThat(entries.get(0).rateType()).isEqualTo(StandbyRateType.SUNDAY_HOLIDAY);

        // Apr 15 Tue — end day 8h, WEEKDAY_SATURDAY
        assertThat(entries.get(1).date()).isEqualTo(LocalDate.of(2025, 4, 15));
        assertThat(entries.get(1).rateType()).isEqualTo(StandbyRateType.WEEKDAY_SATURDAY);
        assertThat(entries.get(1).hours()).isEqualByComparingTo(hours(8));
    }

    // ── Test 3 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("publicHolidayChangesRateType — Koningsdag Apr 27 gets SUNDAY_HOLIDAY")
    void publicHolidayChangesRateType() {
        LocalDateTime start = LocalDateTime.of(2025, 4, 27, 8, 0); // Sunday (Koningsdag)
        LocalDateTime end   = LocalDateTime.of(2025, 4, 28, 8, 0); // Monday
        long periodId = 3L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, Instant.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidayOverrides();
        when(publicHolidayGateway.isHoliday(LocalDate.of(2025, 4, 27))).thenReturn(true);
        when(publicHolidayGateway.isHoliday(LocalDate.of(2025, 4, 28))).thenReturn(false);
        givenNoExistingEntries();
        givenSaveAllReturnsEntries();

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        List<OnCallDayEntryResponse> entries = result.entries();
        assertThat(entries).hasSize(2);

        assertThat(entries.get(0).date()).isEqualTo(LocalDate.of(2025, 4, 27));
        assertThat(entries.get(0).rateType()).isEqualTo(StandbyRateType.SUNDAY_HOLIDAY);

        assertThat(entries.get(1).date()).isEqualTo(LocalDate.of(2025, 4, 28));
        assertThat(entries.get(1).rateType()).isEqualTo(StandbyRateType.WEEKDAY_SATURDAY);
    }

    // ── Test 4 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("workingDayCappedAt15Hours — Mon 08:00 to Tue 08:00: Mon start has 16h raw, capped to 15")
    void workingDayCappedAt15Hours() {
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 8, 0);  // Monday
        LocalDateTime end   = LocalDateTime.of(2025, 4, 15, 8, 0);  // Tuesday
        long periodId = 4L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, Instant.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidayOverrides();
        givenNoPublicHolidays();
        givenNoExistingEntries();
        givenSaveAllReturnsEntries();

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        List<OnCallDayEntryResponse> entries = result.entries();
        assertThat(entries).hasSize(2);

        // Mon start: raw = 24-8 = 16h → capped to 15h
        assertThat(entries.get(0).date()).isEqualTo(LocalDate.of(2025, 4, 14));
        assertThat(entries.get(0).hours()).isEqualByComparingTo(hours(15));
        assertThat(entries.get(0).capped()).isTrue();
    }

    // ── Test 5 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("nonWorkingDayNotCapped — Saturday in Mon-Mon period has 24h and is not capped")
    void nonWorkingDayNotCapped() {
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 14, 0); // Monday
        LocalDateTime end   = LocalDateTime.of(2025, 4, 21, 14, 0); // Monday
        long periodId = 5L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, Instant.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidayOverrides();
        givenNoPublicHolidays();
        givenNoExistingEntries();
        givenSaveAllReturnsEntries();

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        List<OnCallDayEntryResponse> entries = result.entries();
        // Apr 19 Sat is index 5
        OnCallDayEntryResponse satEntry = entries.stream()
                .filter(e -> e.date().equals(LocalDate.of(2025, 4, 19)))
                .findFirst()
                .orElseThrow();

        assertThat(satEntry.hours()).isEqualByComparingTo(hours(24));
        assertThat(satEntry.capped()).isFalse();
        assertThat(satEntry.rateType()).isEqualTo(StandbyRateType.WEEKDAY_SATURDAY);
    }

    // ── Test 6 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("singleDaySameDayPeriod — Mon 09:00 to Mon 17:00 produces 1 entry of 8h")
    void singleDaySameDayPeriod() {
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 9, 0);  // Monday
        LocalDateTime end   = LocalDateTime.of(2025, 4, 14, 17, 0); // Same Monday
        long periodId = 6L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, Instant.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidayOverrides();
        givenNoPublicHolidays();
        givenNoExistingEntries();
        givenSaveAllReturnsEntries();

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        assertThat(result.entries()).hasSize(1);
        OnCallDayEntryResponse entry = result.entries().get(0);
        assertThat(entry.date()).isEqualTo(LocalDate.of(2025, 4, 14));
        assertThat(entry.hours()).isEqualByComparingTo(hours(8));
        assertThat(entry.rateType()).isEqualTo(StandbyRateType.WEEKDAY_SATURDAY);
        assertThat(entry.capped()).isFalse();
        assertThat(entry.timeForTimeFlag()).isFalse();
        assertThat(entry.manualOverride()).isFalse();
    }

    // ── Test 7 ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("throwsWhenPeriodNotFound — raises InvalidOnCallPeriodException")
    void throwsWhenPeriodNotFound() {
        when(onCallPeriodGateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new CalculateOnCallDayEntriesRequest(99L)))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessageContaining("99");
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private void assertEntry(OnCallDayEntryResponse entry, LocalDate date,
                              BigDecimal hours, StandbyRateType rateType, boolean capped) {
        assertThat(entry.date()).isEqualTo(date);
        assertThat(entry.hours()).isEqualByComparingTo(hours);
        assertThat(entry.rateType()).isEqualTo(rateType);
        assertThat(entry.capped()).isEqualTo(capped);
        assertThat(entry.timeForTimeFlag()).isFalse();
        assertThat(entry.manualOverride()).isFalse();
    }
}
