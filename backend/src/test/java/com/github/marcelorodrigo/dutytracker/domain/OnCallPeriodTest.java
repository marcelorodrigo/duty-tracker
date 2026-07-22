package com.github.marcelorodrigo.dutytracker.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OnCallPeriodTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, Month.JULY, 21, 18, 0);

    @Test
    @DisplayName("should create a period with the minimum supported duration")
    void shouldCreatePeriodWithMinimumSupportedDuration() {
        // given
        var end = START.plusHours(1);

        // when
        var period = OnCallPeriod.create(START, end, START.minusDays(1));

        // then
        assertThat(period.id()).isNull();
        assertThat(period.endDateTime()).isEqualTo(end);
    }

    @ParameterizedTest
    @DisplayName("should reject a period shorter than one hour")
    @ValueSource(longs = {-1L, 0L, 59L})
    void shouldRejectPeriodShorterThanOneHour(long minutesAfterStart) {
        // given
        var end = START.plusMinutes(minutesAfterStart);
        var createdAt = START.minusDays(1);

        // when / then
        assertThatThrownBy(() -> OnCallPeriod.create(START, end, createdAt))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessage("Period must be at least 1 hour");
    }

    @Test
    @DisplayName("should reject a period without a start")
    void shouldRejectPeriodWithoutStart() {
        // given
        var end = START.plusHours(1);
        var createdAt = START.minusDays(1);

        // when / then
        assertThatThrownBy(() -> OnCallPeriod.create(null, end, createdAt))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessage("startDateTime and endDateTime are required");
    }

    @Test
    @DisplayName("should preserve period identity when rescheduled")
    void shouldPreservePeriodIdentityWhenRescheduled() {
        // given
        var createdAt = START.minusDays(1);
        var period = new OnCallPeriod(4L, START, START.plusHours(8), createdAt);

        // when
        var updated = period.reschedule(START.plusDays(1), START.plusDays(1).plusHours(8));

        // then
        assertThat(updated.id()).isEqualTo(period.id());
        assertThat(updated.createdAt()).isEqualTo(period.createdAt());
    }
}
