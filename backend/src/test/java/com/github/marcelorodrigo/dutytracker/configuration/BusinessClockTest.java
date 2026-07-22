package com.github.marcelorodrigo.dutytracker.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BusinessClockTest {

    @Test
    @DisplayName("should configure Amsterdam for both business time beans")
    void shouldConfigureAmsterdamForBothBusinessTimeBeans() {
        // given
        var configuration = new BusinessClock();

        // when
        var businessZone = configuration.getBusinessZone();
        var clockZone = configuration.getClock().getZone();

        // then
        assertThat(businessZone).hasToString("Europe/Amsterdam");
        assertThat(clockZone).isEqualTo(businessZone);
    }
}
