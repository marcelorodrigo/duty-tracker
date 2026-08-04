package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    @DisplayName("should throw InvalidIncidentException when incident id is not positive")
    void shouldThrowInvalidIncidentExceptionWhenIncidentIdIsNotPositive(Long incidentId) {
        // given
        var request = new CalculateOvertimeEntriesRequest(incidentId);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("Incident id must be a positive number");
    }
}
