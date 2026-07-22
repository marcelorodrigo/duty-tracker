package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GenerateOnCallPeriodReportRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class GenerateOnCallPeriodReportValidatorTest {

    private final GenerateOnCallPeriodReportValidator validator = new GenerateOnCallPeriodReportValidator();

    @Test
    @DisplayName("should pass validation when period id is positive")
    void shouldPassValidationWhenPeriodIdIsPositive() {
        // given
        var request = new GenerateOnCallPeriodReportRequest(1L);

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should reject null request")
    void shouldRejectNullRequest() {
        // given
        GenerateOnCallPeriodReportRequest request = null;

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessage("request must not be null");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    @DisplayName("should reject period id when it is not positive")
    void shouldRejectPeriodIdWhenItIsNotPositive(Long periodId) {
        // given
        var request = new GenerateOnCallPeriodReportRequest(periodId);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessage("periodId must be a positive number");
    }
}
