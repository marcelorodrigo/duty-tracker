package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.StandbyRateType;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateOnCallDayEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CalculateOnCallDayEntriesValidator;
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
class CalculateOnCallDayEntriesUseCaseTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    HolidayGateway holidayGateway;

    @Mock
    EngineerProfileGateway engineerProfileGateway;

    @Mock
    CalculateOnCallDayEntriesValidator validator;

    CalculateOnCallDayEntriesUseCase useCase;

    // Mon–Fri working days, 09:00–17:00
    private static final EngineerProfile PROFILE = new EngineerProfile(
            1L,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            LocalTime.of(9, 0),
            LocalTime.of(17, 0),
            LocalDateTime.now());

    @BeforeEach
    void setUp() {
        useCase = new CalculateOnCallDayEntriesUseCase(
                onCallPeriodGateway, holidayGateway, engineerProfileGateway, validator);
    }

    private void givenNoHolidays() {
        when(holidayGateway.findByOnCallPeriodId(any())).thenReturn(List.of());
    }

    private static BigDecimal hours(double h) {
        return BigDecimal.valueOf(h).setScale(4, RoundingMode.HALF_UP);
    }

    @Test
    @DisplayName(
            "fullMonToMonWeekNoHolidays — Mon 14:00 to following Mon 14:00 produces 8 entries with correct hours and caps")
    void fullMonToMonWeekNoHolidays() {
        // Mon Apr 14 14:00 → Mon Apr 21 14:00
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 14, 0); // Monday
        LocalDateTime end = LocalDateTime.of(2025, 4, 21, 14, 0); // Monday
        long periodId = 1L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays();

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        assertThat(result.periodId()).isEqualTo(periodId);
        List<OnCallDayEntryResponse> entries = result.entries();
        assertThat(entries).hasSize(8);

        // Apr 14 Mon — start day, working day: pre=max(0,9-14)=0, post=24-17=7h → 7h, not capped
        assertEntry(entries.getFirst(), LocalDate.of(2025, 4, 14), hours(7), StandbyRateType.WEEKDAY_SATURDAY, false);

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

        // Apr 21 Mon — end day, working day: pre=min(14,9)=9h, post=max(0,14-17)=0h → 9h, not capped
        assertEntry(entries.get(7), LocalDate.of(2025, 4, 21), hours(9), StandbyRateType.WEEKDAY_SATURDAY, false);
    }


    @Test
    @DisplayName("holidayChangesRateTypeToSundayHoliday — holiday on Mon changes it to SUNDAY_HOLIDAY")
    void holidayChangesRateTypeToSundayHoliday() {
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 8, 0); // Monday
        LocalDateTime end = LocalDateTime.of(2025, 4, 15, 8, 0); // Tuesday
        long periodId = 2L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(holidayGateway.findByOnCallPeriodId(periodId))
                .thenReturn(List.of(new Holiday(1L, periodId, LocalDate.of(2025, 4, 14), "Liberation Day")));

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        List<OnCallDayEntryResponse> entries = result.entries();
        assertThat(entries).hasSize(2);

        // Apr 14 Mon — holiday → SUNDAY_HOLIDAY, dayLabel = "Holiday", start at 08:00 → 24-8 = 16h, not capped
        assertThat(entries.getFirst().date()).isEqualTo(LocalDate.of(2025, 4, 14));
        assertThat(entries.getFirst().rateType()).isEqualTo(StandbyRateType.SUNDAY_HOLIDAY);
        assertThat(entries.getFirst().dayLabel()).isEqualTo("Holiday");
        assertThat(entries.getFirst().hours()).isEqualByComparingTo(hours(16));
        assertThat(entries.getFirst().capped()).isFalse();

        // Apr 15 Tue — end day 8h, WEEKDAY_SATURDAY
        assertThat(entries.get(1).date()).isEqualTo(LocalDate.of(2025, 4, 15));
        assertThat(entries.get(1).rateType()).isEqualTo(StandbyRateType.WEEKDAY_SATURDAY);
        assertThat(entries.get(1).hours()).isEqualByComparingTo(hours(8));
    }


    @Test
    @DisplayName("holidayOnMiddleDay — holiday on Wed (working day) produces 24h with SUNDAY_HOLIDAY rate")
    void holidayOnMiddleDay() {
        // Mon Apr 14 14:00 → Fri Apr 18 14:00, with holiday on Apr 16 (Wed)
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 14, 0); // Monday
        LocalDateTime end = LocalDateTime.of(2025, 4, 18, 14, 0); // Friday
        long periodId = 3L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        when(holidayGateway.findByOnCallPeriodId(periodId))
                .thenReturn(List.of(new Holiday(1L, periodId, LocalDate.of(2025, 4, 16), "Midweek Holiday")));

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        List<OnCallDayEntryResponse> entries = result.entries();
        assertThat(entries).hasSize(5);

        // Apr 16 Wed — holiday, full 24h, SUNDAY_HOLIDAY rate, not capped
        OnCallDayEntryResponse holidayEntry = entries.stream()
                .filter(e -> e.date().equals(LocalDate.of(2025, 4, 16)))
                .findFirst()
                .orElseThrow();

        assertThat(holidayEntry.hours()).isEqualByComparingTo(hours(24));
        assertThat(holidayEntry.rateType()).isEqualTo(StandbyRateType.SUNDAY_HOLIDAY);
        assertThat(holidayEntry.dayLabel()).isEqualTo("Holiday");
        assertThat(holidayEntry.capped()).isFalse();
    }


    @Test
    @DisplayName(
            "should compute partial start day hours on working day excluding working window — starts at 08:00 yields 8h")
    void shouldComputePartialStartDayHoursOnWorkingDayExcludingWorkingWindow() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 8, 0); // Monday, before work starts at 09:00
        LocalDateTime end = LocalDateTime.of(2025, 4, 15, 8, 0); // Tuesday
        long periodId = 4L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays();

        // when
        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        // then
        // Mon start at 08:00: pre-work = max(0, 9-8) = 1h, post-work = 24-17 = 7h → 8h, not capped
        List<OnCallDayEntryResponse> entries = result.entries();
        assertThat(entries).hasSize(2);
        assertThat(entries.getFirst().date()).isEqualTo(LocalDate.of(2025, 4, 14));
        assertThat(entries.getFirst().hours()).isEqualByComparingTo(hours(8));
        assertThat(entries.getFirst().capped()).isFalse();
    }


    @Test
    @DisplayName("nonWorkingDayNotCapped — Saturday in Mon-Mon period has 24h and is not capped")
    void nonWorkingDayNotCapped() {
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 14, 0); // Monday
        LocalDateTime end = LocalDateTime.of(2025, 4, 21, 14, 0); // Monday
        long periodId = 5L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays();

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        OnCallDayEntryResponse satEntry = result.entries().stream()
                .filter(e -> e.date().equals(LocalDate.of(2025, 4, 19)))
                .findFirst()
                .orElseThrow();

        assertThat(satEntry.hours()).isEqualByComparingTo(hours(24));
        assertThat(satEntry.capped()).isFalse();
        assertThat(satEntry.rateType()).isEqualTo(StandbyRateType.WEEKDAY_SATURDAY);
    }


    @Test
    @DisplayName("singleDaySameDayPeriod — Mon 09:00 to Mon 17:00 produces 1 entry of 8h")
    void singleDaySameDayPeriod() {
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 9, 0); // Monday
        LocalDateTime end = LocalDateTime.of(2025, 4, 14, 17, 0); // Same Monday
        long periodId = 6L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays();

        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        assertThat(result.entries()).hasSize(1);
        OnCallDayEntryResponse entry = result.entries().getFirst();
        assertThat(entry.date()).isEqualTo(LocalDate.of(2025, 4, 14));
        assertThat(entry.hours()).isEqualByComparingTo(hours(8));
        assertThat(entry.rateType()).isEqualTo(StandbyRateType.WEEKDAY_SATURDAY);
        assertThat(entry.capped()).isFalse();
    }


    @Test
    @DisplayName("throwsWhenPeriodNotFound — raises InvalidOnCallPeriodException")
    void throwsWhenPeriodNotFound() {
        when(onCallPeriodGateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new CalculateOnCallDayEntriesRequest(99L)))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessageContaining("99");
    }


    @Test
    @DisplayName("should compute start day hours on working day when on-call starts after work ends")
    void shouldComputeStartDayHoursOnWorkingDayWhenOnCallStartsAfterWorkEnds() {
        // given — on-call starts at 20:00 on Monday (after work ends at 17:00)
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 20, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 15, 8, 0);
        long periodId = 7L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays();

        // when
        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        // then
        // Mon start at 20:00: pre=max(0,9-20)=0, post=max(0,24-20)=4h → 4h, not capped
        assertThat(result.entries().getFirst().hours()).isEqualByComparingTo(hours(4));
        assertThat(result.entries().getFirst().capped()).isFalse();
    }


    @Test
    @DisplayName("should compute end day hours on working day when on-call ends after work ends")
    void shouldComputeEndDayHoursOnWorkingDayWhenOnCallEndsAfterWorkEnds() {
        // given — on-call ends at 18:00 on Monday (after work ends at 17:00)
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 20, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 15, 18, 0); // Tuesday
        long periodId = 8L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays();

        // when
        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        // then
        // Tue end at 18:00: pre=min(18,9)=9h, post=max(0,18-17)=1h → 10h, not capped
        OnCallDayEntryResponse endEntry = result.entries().get(1);
        assertThat(endEntry.date()).isEqualTo(LocalDate.of(2025, 4, 15));
        assertThat(endEntry.hours()).isEqualByComparingTo(hours(10));
        assertThat(endEntry.capped()).isFalse();
    }


    @Test
    @DisplayName("should compute end day hours on working day when on-call ends within working hours")
    void shouldComputeEndDayHoursOnWorkingDayWhenOnCallEndsWithinWorkingHours() {
        // given — on-call ends at 14:00 on Monday (within working hours 09:00–17:00)
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 20, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 15, 14, 0); // Tuesday
        long periodId = 9L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays();

        // when
        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        // then
        // Tue end at 14:00: pre=min(14,9)=9h, post=max(0,14-17)=0h → 9h, not capped
        OnCallDayEntryResponse endEntry = result.entries().get(1);
        assertThat(endEntry.date()).isEqualTo(LocalDate.of(2025, 4, 15));
        assertThat(endEntry.hours()).isEqualByComparingTo(hours(9));
        assertThat(endEntry.capped()).isFalse();
    }


    @Test
    @DisplayName("should compute end day hours on working day when on-call ends before working hours start")
    void shouldComputeEndDayHoursOnWorkingDayWhenOnCallEndsBeforeWorkingHoursStart() {
        // given — on-call ends at 07:00 on Monday (before work starts at 09:00)
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 20, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 15, 7, 0); // Tuesday
        long periodId = 10L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays();

        // when
        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        // then
        // Tue end at 07:00: pre=min(7,9)=7h, post=max(0,7-17)=0h → 7h, not capped
        OnCallDayEntryResponse endEntry = result.entries().get(1);
        assertThat(endEntry.date()).isEqualTo(LocalDate.of(2025, 4, 15));
        assertThat(endEntry.hours()).isEqualByComparingTo(hours(7));
        assertThat(endEntry.capped()).isFalse();
    }


    @Test
    @DisplayName("should not cap partial start day on working day even when raw hours would exceed 15")
    void shouldNotCapPartialStartDayOnWorkingDayEvenWhenRawHoursWouldExceed15() {
        // given — on-call starts at 00:00 on Monday (profile 09:00–17:00 → 16h outside work)
        // Policy: cap only applies to full middle days, not partial start/end days
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 15, 8, 0);
        long periodId = 11L;
        OnCallPeriod period = new OnCallPeriod(periodId, start, end, LocalDateTime.now());

        when(onCallPeriodGateway.findById(periodId)).thenReturn(Optional.of(period));
        when(engineerProfileGateway.find()).thenReturn(Optional.of(PROFILE));
        givenNoHolidays();

        // when
        var result = useCase.execute(new CalculateOnCallDayEntriesRequest(periodId));

        // then
        // Mon start at 00:00: pre=max(0,9-0)=9h, post=24-17=7h → 16h, not capped
        assertThat(result.entries().getFirst().hours()).isEqualByComparingTo(hours(16));
        assertThat(result.entries().getFirst().capped()).isFalse();
    }


    private void assertEntry(
            OnCallDayEntryResponse entry, LocalDate date, BigDecimal hours, StandbyRateType rateType, boolean capped) {
        assertThat(entry.date()).isEqualTo(date);
        assertThat(entry.hours()).isEqualByComparingTo(hours);
        assertThat(entry.rateType()).isEqualTo(rateType);
        assertThat(entry.capped()).isEqualTo(capped);
    }
}
