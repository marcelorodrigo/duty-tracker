package com.github.marcelorodrigo.dutytracker.usecase.validator.compensation;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCompensationRateException;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.UpdateCompensationRateRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateCompensationRateValidatorTest {

    private final UpdateCompensationRateValidator validator = new UpdateCompensationRateValidator();

    @Test
    @DisplayName("should throw invalid compensation rate exception when rateId is null")
    void shouldThrowInvalidCompensationRateExceptionWhenRateIdIsNull() {
        // given
        var request = new UpdateCompensationRateRequest(null, BigDecimal.TEN, "Label");

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidCompensationRateException.class)
                .hasMessage("rateId is required");
    }

    @Test
    @DisplayName("should throw invalid compensation rate exception when percentage is null")
    void shouldThrowInvalidCompensationRateExceptionWhenPercentageIsNull() {
        // given
        var request = new UpdateCompensationRateRequest(1L, null, "Label");

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidCompensationRateException.class)
                .hasMessage("percentage must be >= 0");
    }

    @Test
    @DisplayName("should throw invalid compensation rate exception when percentage is negative")
    void shouldThrowInvalidCompensationRateExceptionWhenPercentageIsNegative() {
        // given
        var request = new UpdateCompensationRateRequest(1L, new BigDecimal("-0.01"), "Label");

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidCompensationRateException.class)
                .hasMessage("percentage must be >= 0");
    }

    @Test
    @DisplayName("should pass validation when request is valid")
    void shouldPassValidationWhenRequestIsValid() {
        // given
        var request = new UpdateCompensationRateRequest(1L, BigDecimal.TEN, "Label");

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }
}
