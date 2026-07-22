package com.github.marcelorodrigo.dutytracker.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IncidentTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, Month.JULY, 21, 18, 0);

    @Test
    @DisplayName("should create an incident with the minimum supported duration")
    void shouldCreateIncidentWithMinimumSupportedDuration() {
        // given
        var end = START.plusMinutes(1);

        // when
        var incident = Incident.create(1L, "Database outage", START, end, START.minusDays(1));

        // then
        assertThat(incident.id()).isNull();
        assertThat(incident.endDateTime()).isEqualTo(end);
    }

    @ParameterizedTest
    @DisplayName("should reject an incident without a positive on-call period id")
    @ValueSource(longs = {0L, -1L})
    void shouldRejectIncidentWithoutPositiveOnCallPeriodId(long onCallPeriodId) {
        // given
        var end = START.plusHours(1);
        var createdAt = START.minusDays(1);

        // when / then
        assertThatThrownBy(() -> Incident.create(onCallPeriodId, "Database outage", START, end, createdAt))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("onCallPeriodId must be a positive number");
    }

    @Test
    @DisplayName("should reject an incident without a name")
    void shouldRejectIncidentWithoutName() {
        // given
        var end = START.plusHours(1);
        var createdAt = START.minusDays(1);

        // when / then
        assertThatThrownBy(() -> Incident.create(1L, " ", START, end, createdAt))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("name is required");
    }

    @ParameterizedTest
    @DisplayName("should reject an incident shorter than one minute")
    @ValueSource(longs = {-1L, 0L, 59L})
    void shouldRejectIncidentShorterThanOneMinute(long secondsAfterStart) {
        // given
        var end = START.plusSeconds(secondsAfterStart);
        var createdAt = START.minusDays(1);

        // when / then
        assertThatThrownBy(() -> Incident.create(1L, "Database outage", START, end, createdAt))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("Incident endDateTime must be at least 1 minute after startDateTime");
    }

    @Test
    @DisplayName("should preserve incident identity when details change")
    void shouldPreserveIncidentIdentityWhenDetailsChange() {
        // given
        var createdAt = START.minusDays(1);
        var incident = new Incident(9L, 3L, "Old name", START, START.plusHours(1), createdAt);

        // when
        var updated = incident.withDetails("New name", START.plusHours(1), START.plusHours(2));

        // then
        assertThat(updated.id()).isEqualTo(incident.id());
        assertThat(updated.onCallPeriodId()).isEqualTo(incident.onCallPeriodId());
        assertThat(updated.createdAt()).isEqualTo(incident.createdAt());
    }
}
