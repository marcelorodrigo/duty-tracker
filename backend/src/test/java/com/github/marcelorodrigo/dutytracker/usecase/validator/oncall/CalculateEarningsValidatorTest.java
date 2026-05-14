package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateEarningsRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculateEarningsValidatorTest {

    private final CalculateEarningsValidator validator = new CalculateEarningsValidator();

    @Test
    @DisplayName("should throw exception when periodId is null")
    void shouldThrowExceptionWhenPeriodIdIsNull() {
        // given
        var request = new CalculateEarningsRequest(null);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessageContaining("periodId must not be null");
    }

    @Test
    @DisplayName("should pass validation when periodId is provided")
    void shouldPassValidationWhenPeriodIdIsProvided() {
        // given
        var request = new CalculateEarningsRequest(1L);

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
