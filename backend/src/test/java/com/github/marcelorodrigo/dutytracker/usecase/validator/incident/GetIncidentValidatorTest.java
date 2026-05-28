package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.GetIncidentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GetIncidentValidatorTest {

    private final GetIncidentValidator validator = new GetIncidentValidator();

    @Test
    @DisplayName("should pass validation when id is a positive number")
    void shouldPassValidationWhenIdIsAPositiveNumber() {
        // given
        var request = new GetIncidentRequest(1L);

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should throw InvalidIncidentException when id is null")
    void shouldThrowInvalidIncidentExceptionWhenIdIsNull() {
        // given
        var request = new GetIncidentRequest(null);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident id must be a positive number");
    }

    @ParameterizedTest
    @DisplayName("should throw InvalidIncidentException when id is zero or negative")
    @ValueSource(longs = {0L, -1L, -100L})
    void shouldThrowInvalidIncidentExceptionWhenIdIsZeroOrNegative(long id) {
        // given
        var request = new GetIncidentRequest(id);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident id must be a positive number");
    }
}
