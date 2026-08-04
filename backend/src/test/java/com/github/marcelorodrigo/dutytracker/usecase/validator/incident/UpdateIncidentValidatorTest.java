package com.github.marcelorodrigo.dutytracker.usecase.validator.incident;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateIncidentValidatorTest {

    @Mock
    private IncidentGateway incidentGateway;

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private Clock clock;

    @InjectMocks
    private UpdateIncidentValidator validator;

    @Test
    @DisplayName("should throw IncidentNotFoundException when incident is not found")
    void shouldThrowIncidentNotFoundExceptionWhenIncidentIsNotFound() {
        // given
        var request = requestWithId(99L);
        when(incidentGateway.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(IncidentNotFoundException.class)
                .hasMessage("Incident not found: 99");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    @DisplayName("should throw InvalidIncidentException when incident id is not positive")
    void shouldThrowInvalidIncidentExceptionWhenIncidentIdIsNotPositive(Long incidentId) {
        // given
        var request = requestWithId(incidentId);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("Incident id must be a positive number");
        verifyNoInteractions(incidentGateway, onCallPeriodGateway, clock);
    }

    private static UpdateIncidentRequest requestWithId(Long incidentId) {
        return new UpdateIncidentRequest(
                incidentId,
                "Network outage",
                LocalDateTime.of(2026, Month.JULY, 20, 9, 0),
                LocalDateTime.of(2026, Month.JULY, 20, 10, 0));
    }
}
