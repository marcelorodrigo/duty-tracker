package com.github.marcelorodrigo.dutytracker.usecase.validator.profile;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidStandbyPercentageException;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.CreateEngineerProfileRequest;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateEngineerProfileValidatorTest {

    private final CreateEngineerProfileValidator validator = new CreateEngineerProfileValidator();

    private CreateEngineerProfileRequest validRequest(BigDecimal weekdaySat, BigDecimal sundayHol) {
        return new CreateEngineerProfileRequest(
                Set.of(DayOfWeek.MONDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                new BigDecimal("50.00"),
                weekdaySat,
                sundayHol);
    }

    @Test
    @DisplayName("should accept null standby percentages")
    void shouldAcceptNullStandbyPercentages() {
        // given
        var request = validRequest(null, null);

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    @DisplayName("should accept valid standby weekday saturday percentage")
    void shouldAcceptValidWeekdaySaturdayPercentage() {
        // given
        var request = validRequest(new BigDecimal("0.067"), null);

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    @DisplayName("should throw when standby weekday saturday percentage is below 0.001")
    void shouldThrowWhenWeekdaySaturdayPercentageBelowMinimum() {
        CreateEngineerProfileRequest request = validRequest(new BigDecimal("0.0009"), null);

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidStandbyPercentageException.class)
                .hasMessageContaining("standbyWeekdaySaturdayPercentage");
    }

    @Test
    @DisplayName("should throw when standby sunday holiday percentage is below 0.001")
    void shouldThrowWhenSundayHolidayPercentageBelowMinimum() {
        CreateEngineerProfileRequest request = validRequest(null, new BigDecimal("0.0009"));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidStandbyPercentageException.class)
                .hasMessageContaining("standbyWeekdaySundayHolidayPercentage");
    }

    @Test
    @DisplayName("should throw when standby weekend saturday percentage is zero")
    void shouldThrowWhenWeekdaySaturdayPercentageIsZero() {
        CreateEngineerProfileRequest request = validRequest(BigDecimal.ZERO, null);

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(InvalidStandbyPercentageException.class);
    }

    @Test
    @DisplayName("should accept standby percentage at exactly 0.001")
    void shouldAcceptStandbyPercentageAtMinimum() {
        // given
        var request = validRequest(new BigDecimal("0.001"), new BigDecimal("0.001"));

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }
}
