package com.github.marcelorodrigo.dutytracker.gateway.postgres.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EngineerProfileEntityTest {

    private static final Set<DayOfWeek> WORKING_DAYS =
            EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    @Test
    @DisplayName("should set all fields correctly via the full constructor")
    void shouldSetAllFieldsCorrectlyViaTheFullConstructor() {
        // given / when
        var entity = new EngineerProfileEntity(
                1L,
                WORKING_DAYS,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                new BigDecimal("50.00"),
                new BigDecimal("15.000"),
                new BigDecimal("30.000"));

        // then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getWorkingDays()).isEqualTo(WORKING_DAYS);
        assertThat(entity.getWorkStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(entity.getWorkEndTime()).isEqualTo(LocalTime.of(17, 0));
        assertThat(entity.getHourlyRate()).isEqualByComparingTo("50.00");
        assertThat(entity.getStandbyWeekdaySaturdayPercentage()).isEqualByComparingTo("15.000");
        assertThat(entity.getStandbyWeekdaySundayHolidayPercentage()).isEqualByComparingTo("30.000");
    }

    @Test
    @DisplayName("should allow null createdAt before persistence")
    void shouldAllowNullCreatedAtBeforePersistence() {
        // given / when
        var entity = new EngineerProfileEntity(
                null,
                WORKING_DAYS,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                new BigDecimal("50.00"),
                new BigDecimal("15.000"),
                new BigDecimal("30.000"));

        // then
        assertThat(entity.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("should update profile details without changing its identity")
    void shouldUpdateProfileDetailsWithoutChangingItsIdentity() {
        // given
        var entity = new EngineerProfileEntity(
                42L,
                WORKING_DAYS,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                new BigDecimal("50.00"),
                new BigDecimal("15.000"),
                new BigDecimal("30.000"));

        // when
        entity.updateDetails(
                WORKING_DAYS,
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                new BigDecimal("75.00"),
                new BigDecimal("20.000"),
                new BigDecimal("40.000"));

        // then
        assertThat(entity.getId()).isEqualTo(42L);
        assertThat(entity.getHourlyRate()).isEqualByComparingTo("75.00");
        assertThat(entity.getWorkStartTime()).isEqualTo(LocalTime.of(8, 0));
    }
}
