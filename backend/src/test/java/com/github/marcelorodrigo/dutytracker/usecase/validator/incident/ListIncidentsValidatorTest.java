package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.ListIncidentsRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListIncidentsValidatorTest {

    private final ListIncidentsValidator validator = new ListIncidentsValidator();

    @Test
    @DisplayName("should accept a list incidents request")
    void shouldAcceptListIncidentsRequest() {
        // given
        var request = new ListIncidentsRequest(1L, new PaginationRequest(0, 20, List.of()));

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
