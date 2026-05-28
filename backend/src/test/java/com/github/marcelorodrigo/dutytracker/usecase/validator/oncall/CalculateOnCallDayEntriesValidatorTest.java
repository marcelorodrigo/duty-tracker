package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateOnCallDayEntriesRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculateOnCallDayEntriesValidatorTest {

    private final CalculateOnCallDayEntriesValidator validator = new CalculateOnCallDayEntriesValidator();

    @Test
    @DisplayName("should pass validation when periodId is provided")
    void shouldPassValidationWhenPeriodIdIsProvided() {
        // given
        var request = new CalculateOnCallDayEntriesRequest(1L);

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when periodId is null")
    void shouldThrowInvalidOnCallPeriodExceptionWhenPeriodIdIsNull() {
        // given
        var request = new CalculateOnCallDayEntriesRequest(null);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessageContaining("periodId must not be null");
    }
}
