package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateOnCallPeriodRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateOnCallPeriodValidatorTest {

    private final UpdateOnCallPeriodValidator validator = new UpdateOnCallPeriodValidator();

    private static final Long PERIOD_ID = 42L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 5, 11, 14, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 5, 18, 14, 0);

    @Test
    @DisplayName("should pass validation when period range is valid")
    void shouldPassValidationWhenPeriodRangeIsValid() {
        // given
        var request = new UpdateOnCallPeriodRequest(PERIOD_ID, START, END);

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when end is before start")
    void shouldThrowWhenEndIsBeforeStart() {
        var request = new UpdateOnCallPeriodRequest(PERIOD_ID, END, START);
        assertThatExceptionOfType(InvalidOnCallPeriodException.class)
                .isThrownBy(() -> validator.validate(request))
                .withMessage("endDateTime must be after startDateTime");
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when period is less than 1 hour")
    void shouldThrowWhenPeriodIsLessThanOneHour() {
        LocalDateTime almostOneHour = START.plusMinutes(30);

        var request = new UpdateOnCallPeriodRequest(PERIOD_ID, START, almostOneHour);
        assertThatExceptionOfType(InvalidOnCallPeriodException.class)
                .isThrownBy(() -> validator.validate(request))
                .withMessage("Period must be at least 1 hour");
    }
}
