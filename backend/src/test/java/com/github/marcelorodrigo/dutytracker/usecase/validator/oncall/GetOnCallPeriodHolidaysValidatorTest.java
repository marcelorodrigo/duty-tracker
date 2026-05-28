package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetOnCallPeriodHolidaysRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetOnCallPeriodHolidaysValidatorTest {

    private final GetOnCallPeriodHolidaysValidator validator = new GetOnCallPeriodHolidaysValidator();

    @Test
    @DisplayName("should not throw any exception when request is valid")
    void shouldNotThrowAnyExceptionWhenRequestIsValid() {
        // given
        var request = new GetOnCallPeriodHolidaysRequest(1L);

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
