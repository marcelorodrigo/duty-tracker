package com.github.marcelorodrigo.dutytracker.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HoursTest {

    @Test
    @DisplayName("should convert minutes using the shared hours precision")
    void shouldConvertMinutesUsingSharedHoursPrecision() {
        // given
        var minutes = 20;

        // when
        var hours = Hours.fromMinutes(minutes);

        // then
        assertThat(hours.value()).isEqualByComparingTo(new BigDecimal("0.3333"));
    }

    @Test
    @DisplayName("should round a partial overtime hour up to a whole hour")
    void shouldRoundPartialOvertimeHourUpToWholeHour() {
        // given
        var minutes = 61;

        // when
        var hours = Hours.roundedUpFromMinutes(minutes);

        // then
        assertThat(hours.value()).isEqualByComparingTo(new BigDecimal("2.0000"));
    }

    @Test
    @DisplayName("should add hour quantities without exposing arithmetic policy")
    void shouldAddHourQuantitiesWithoutExposingArithmeticPolicy() {
        // given
        var first = Hours.fromMinutes(30);
        var second = Hours.fromMinutes(90);

        // when
        var total = first.add(second);

        // then
        assertThat(total.value()).isEqualByComparingTo(new BigDecimal("2.0000"));
        assertThat(total.isPositive()).isTrue();
    }

    @Test
    @DisplayName("should reject a negative duration")
    void shouldRejectNegativeDuration() {
        // given
        var minutes = -1;

        // when / then
        assertThatThrownBy(() -> Hours.fromMinutes(minutes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minutes must be >= 0");
    }
}
