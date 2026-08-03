package com.github.marcelorodrigo.dutytracker.usecase.profile;

import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.usecase.response.profile.EngineerProfileResponse;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EngineerProfileResponseMapperTest {

    private final EngineerProfileResponseMapper mapper = new EngineerProfileResponseMapperImpl();

    @Test
    @DisplayName("should map every profile response field in calendar order")
    void shouldMapEveryProfileResponseFieldInCalendarOrder() {
        // given
        var profile = new EngineerProfile(
                12L,
                Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalTime.of(8, 30),
                LocalTime.of(17, 0),
                new BigDecimal("75.50"),
                new BigDecimal("0.067"),
                new BigDecimal("0.084"),
                FIXED_DATE_TIME);
        var expected = new EngineerProfileResponse(
                12L,
                List.of("MONDAY", "WEDNESDAY", "FRIDAY"),
                LocalTime.of(8, 30),
                LocalTime.of(17, 0),
                new BigDecimal("75.50"),
                new BigDecimal("0.067"),
                new BigDecimal("0.084"));

        // when
        var result = mapper.toResponse(profile);

        // then
        assertThat(result).isEqualTo(expected);
    }
}
