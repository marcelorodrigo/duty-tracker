package com.github.marcelorodrigo.dutytracker.infrastructure.config;

import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_CLOCK;
import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JpaAuditingConfigurationTest {

    private final JpaAuditingConfiguration configuration = new JpaAuditingConfiguration();

    @Test
    @DisplayName("should supply persistence timestamps from the business clock")
    void shouldSupplyPersistenceTimestampsFromTheBusinessClock() {
        // given
        var dateTimeProvider = configuration.businessDateTimeProvider(FIXED_CLOCK);

        // when
        var currentTime = dateTimeProvider.getNow();

        // then
        assertThat(currentTime).contains(FIXED_DATE_TIME);
    }
}
