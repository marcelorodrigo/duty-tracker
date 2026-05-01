package com.github.marcelorodrigo.dutytracker.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BusinessClock")
class BusinessClockTest {

    @Test
    @DisplayName("BUSINESS_ZONE should be Europe/Amsterdam")
    void businessZoneIsEuropeAmsterdam() {
        assertThat(BusinessClock.BUSINESS_ZONE).isEqualTo(ZoneId.of("Europe/Amsterdam"));
    }
}
