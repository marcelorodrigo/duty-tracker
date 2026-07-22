package com.github.marcelorodrigo.dutytracker.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidEngineerProfileException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHourlyRateException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidStandbyPercentageException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EngineerProfileTest {

    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(17, 0);
    private static final BigDecimal HOURLY_RATE = new BigDecimal("50.00");
    private static final BigDecimal WEEKDAY_PERCENTAGE = new BigDecimal("0.067");
    private static final BigDecimal HOLIDAY_PERCENTAGE = new BigDecimal("0.084");

    @Test
    @DisplayName("should protect working days from changes outside the profile")
    void shouldProtectWorkingDaysFromChangesOutsideProfile() {
        // given
        var workingDays = new HashSet<>(Set.of(DayOfWeek.MONDAY));

        // when
        var profile = profile(workingDays, WORK_START, WORK_END, HOURLY_RATE, WEEKDAY_PERCENTAGE);
        workingDays.add(DayOfWeek.TUESDAY);

        // then
        var storedWorkingDays = profile.workingDays();
        assertThat(storedWorkingDays).containsExactly(DayOfWeek.MONDAY);
        assertThatThrownBy(() -> storedWorkingDays.add(DayOfWeek.WEDNESDAY))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should reject a profile without working days")
    void shouldRejectProfileWithoutWorkingDays() {
        // given
        var workingDays = Set.<DayOfWeek>of();

        // when / then
        assertThatThrownBy(() -> profile(workingDays, WORK_START, WORK_END, HOURLY_RATE, WEEKDAY_PERCENTAGE))
                .isInstanceOf(InvalidEngineerProfileException.class)
                .hasMessage("At least one working day must be specified");
    }

    @Test
    @DisplayName("should reject a work schedule without a positive duration")
    void shouldRejectWorkScheduleWithoutPositiveDuration() {
        // given
        var workingDays = Set.of(DayOfWeek.MONDAY);

        // when / then
        assertThatThrownBy(() -> profile(workingDays, WORK_START, WORK_START, HOURLY_RATE, WEEKDAY_PERCENTAGE))
                .isInstanceOf(InvalidEngineerProfileException.class)
                .hasMessage("workEndTime must be after workStartTime");
    }

    @Test
    @DisplayName("should reject an hourly rate below the supported minimum")
    void shouldRejectHourlyRateBelowSupportedMinimum() {
        // given
        var hourlyRate = new BigDecimal("0.99");
        var workingDays = Set.of(DayOfWeek.MONDAY);

        // when / then
        assertThatThrownBy(() -> profile(workingDays, WORK_START, WORK_END, hourlyRate, WEEKDAY_PERCENTAGE))
                .isInstanceOf(InvalidHourlyRateException.class)
                .hasMessage("Hourly rate must be at least 1");
    }

    @Test
    @DisplayName("should accept the aggregate hourly rate minimum")
    void shouldAcceptAggregateHourlyRateMinimum() {
        // given
        var workingDays = Set.of(DayOfWeek.MONDAY);

        // when
        var profile = profile(workingDays, WORK_START, WORK_END, BigDecimal.ONE, WEEKDAY_PERCENTAGE);

        // then
        assertThat(profile.hourlyRate().value()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("should reject a missing hourly rate")
    void shouldRejectMissingHourlyRate() {
        // given
        var workingDays = Set.of(DayOfWeek.MONDAY);

        // when / then
        assertThatThrownBy(() -> new EngineerProfile(
                        null,
                        workingDays,
                        WORK_START,
                        WORK_END,
                        null,
                        Percentage.of(WEEKDAY_PERCENTAGE),
                        Percentage.of(HOLIDAY_PERCENTAGE),
                        null))
                .isInstanceOf(InvalidHourlyRateException.class)
                .hasMessage("Hourly rate must be at least 1");
    }

    @Test
    @DisplayName("should reject a standby percentage below the supported minimum")
    void shouldRejectStandbyPercentageBelowSupportedMinimum() {
        // given
        var percentage = new BigDecimal("0.0009");
        var workingDays = Set.of(DayOfWeek.MONDAY);

        // when / then
        assertThatThrownBy(() -> profile(workingDays, WORK_START, WORK_END, HOURLY_RATE, percentage))
                .isInstanceOf(InvalidStandbyPercentageException.class)
                .hasMessage("standbyWeekdaySaturdayPercentage must be at least 0.001");
    }

    @Test
    @DisplayName("should reject a missing standby weekday saturday percentage")
    void shouldRejectMissingStandbyWeekdaySaturdayPercentage() {
        // given
        var workingDays = Set.of(DayOfWeek.MONDAY);

        // when / then
        assertThatThrownBy(() -> new EngineerProfile(
                        null,
                        workingDays,
                        WORK_START,
                        WORK_END,
                        Money.of(HOURLY_RATE),
                        null,
                        Percentage.of(HOLIDAY_PERCENTAGE),
                        null))
                .isInstanceOf(InvalidStandbyPercentageException.class)
                .hasMessage("standbyWeekdaySaturdayPercentage must be at least 0.001");
    }

    @Test
    @DisplayName("should reject a standby sunday holiday percentage below the minimum")
    void shouldRejectStandbySundayHolidayPercentageBelowMinimum() {
        // given
        var workingDays = Set.of(DayOfWeek.MONDAY);

        // when / then
        assertThatThrownBy(() -> new EngineerProfile(
                        null,
                        workingDays,
                        WORK_START,
                        WORK_END,
                        Money.of(HOURLY_RATE),
                        Percentage.of(WEEKDAY_PERCENTAGE),
                        Percentage.of(new BigDecimal("0.0009")),
                        null))
                .isInstanceOf(InvalidStandbyPercentageException.class)
                .hasMessage("standbyWeekdaySundayHolidayPercentage must be at least 0.001");
    }

    @Test
    @DisplayName("should accept both aggregate standby percentage minima")
    void shouldAcceptBothAggregateStandbyPercentageMinima() {
        // given
        var minimum = new BigDecimal("0.001");
        var workingDays = Set.of(DayOfWeek.MONDAY);

        // when
        var profile = EngineerProfile.create(
                workingDays,
                WORK_START,
                WORK_END,
                Money.of(HOURLY_RATE),
                Percentage.of(minimum),
                Percentage.of(minimum));

        // then
        assertThat(profile.standbyWeekdaySaturdayPercentage().value()).isEqualByComparingTo(minimum);
        assertThat(profile.standbyWeekdaySundayHolidayPercentage().value()).isEqualByComparingTo(minimum);
    }

    @Test
    @DisplayName("should preserve profile identity when settings change")
    void shouldPreserveProfileIdentityWhenSettingsChange() {
        // given
        var createdAt = LocalDateTime.of(2026, Month.JULY, 1, 12, 0);
        var profile = new EngineerProfile(
                7L,
                Set.of(DayOfWeek.MONDAY),
                WORK_START,
                WORK_END,
                Money.of(HOURLY_RATE),
                Percentage.of(WEEKDAY_PERCENTAGE),
                Percentage.of(HOLIDAY_PERCENTAGE),
                createdAt);

        // when
        var updated = profile.withSettings(
                Set.of(DayOfWeek.TUESDAY),
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                Money.of(new BigDecimal("60.00")),
                Percentage.of(WEEKDAY_PERCENTAGE),
                Percentage.of(HOLIDAY_PERCENTAGE));

        // then
        assertThat(updated.id()).isEqualTo(profile.id());
        assertThat(updated.createdAt()).isEqualTo(profile.createdAt());
    }

    private EngineerProfile profile(
            Set<DayOfWeek> workingDays,
            LocalTime workStart,
            LocalTime workEnd,
            BigDecimal hourlyRate,
            BigDecimal weekdayPercentage) {
        return EngineerProfile.create(
                workingDays,
                workStart,
                workEnd,
                Money.of(hourlyRate),
                Percentage.of(weekdayPercentage),
                Percentage.of(HOLIDAY_PERCENTAGE));
    }
}
