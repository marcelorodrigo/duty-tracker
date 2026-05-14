package com.github.marcelorodrigo.dutytracker.usecase.validator.profile;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidStandbyPercentageException;
import com.github.marcelorodrigo.dutytracker.usecase.request.profile.UpdateEngineerProfileRequest;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateEngineerProfileValidatorTest {

    @InjectMocks
    UpdateEngineerProfileValidator validator;

    private UpdateEngineerProfileRequest validRequest(BigDecimal weekdaySat, BigDecimal sundayHol) {
        return new UpdateEngineerProfileRequest(
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
        assertThatNoException().isThrownBy(() -> validator.validate(validRequest(null, null)));
    }

    @Test
    @DisplayName("should throw when standby weekday saturday percentage is below 0.001")
    void shouldThrowWhenWeekdaySaturdayPercentageBelowMinimum() {
        UpdateEngineerProfileRequest request = validRequest(new BigDecimal("0.0009"), null);

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidStandbyPercentageException.class)
                .hasMessageContaining("standbyWeekdaySaturdayPercentage");
    }

    @Test
    @DisplayName("should throw when standby sunday holiday percentage is below 0.001")
    void shouldThrowWhenSundayHolidayPercentageBelowMinimum() {
        UpdateEngineerProfileRequest request = validRequest(null, new BigDecimal("0.0009"));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidStandbyPercentageException.class)
                .hasMessageContaining("standbyWeekdaySundayHolidayPercentage");
    }

    @Test
    @DisplayName("should accept standby percentage at exactly 0.001")
    void shouldAcceptStandbyPercentageAtMinimum() {
        assertThatNoException()
                .isThrownBy(() -> validator.validate(validRequest(new BigDecimal("0.001"), new BigDecimal("0.001"))));
    }
}
