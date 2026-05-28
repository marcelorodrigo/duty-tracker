package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculateOvertimeEntriesValidatorTest {

    private final CalculateOvertimeEntriesValidator validator = new CalculateOvertimeEntriesValidator();

    @Test
    @DisplayName("should not throw any exception when request is valid")
    void shouldNotThrowAnyExceptionWhenRequestIsValid() {
        // given
        var request = new CalculateOvertimeEntriesRequest(1L); // incidentId

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
