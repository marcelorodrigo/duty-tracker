package com.github.marcelorodrigo.dutytracker.usecase.compensation;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompensationRateResponseMapperTest {

    private final CompensationRateResponseMapper mapper = new CompensationRateResponseMapperImpl();

    @Test
    @DisplayName("should map every compensation rate response field")
    void shouldMapEveryCompensationRateResponseField() {
        // given
        var rate = new CompensationRate(
                42L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Night shift",
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                new BigDecimal("150.00"));
        var expected = new CompensationRateResponse(
                42L,
                RateCategory.OVERTIME_ALLOWANCE,
                OvertimeDayType.WEEKDAY,
                "Night shift",
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                new BigDecimal("150.00"));

        // when
        var result = mapper.toResponse(rate);

        // then
        assertThat(result).isEqualTo(expected);
    }
}
