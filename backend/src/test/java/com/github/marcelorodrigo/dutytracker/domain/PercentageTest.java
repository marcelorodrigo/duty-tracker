package com.github.marcelorodrigo.dutytracker.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PercentageTest {

    @Test
    @DisplayName("should expose percentage points as a multiplication factor")
    void shouldExposePercentagePointsAsMultiplicationFactor() {
        // given
        var percentage = Percentage.of(new BigDecimal("35"));

        // when
        var factor = percentage.asFactor();

        // then
        assertThat(factor).isEqualByComparingTo(new BigDecimal("0.35"));
    }
}
