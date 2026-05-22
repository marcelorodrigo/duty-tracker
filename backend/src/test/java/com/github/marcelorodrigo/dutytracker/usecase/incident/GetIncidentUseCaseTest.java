package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.GetIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.GetIncidentValidator;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetIncidentUseCaseTest {

    @Mock
    IncidentGateway incidentGateway;

    @Mock
    GetIncidentValidator validator;

    @InjectMocks
    GetIncidentUseCase useCase;

    private Incident sampleIncident() {
        return new Incident(
                1L,
                10L,
                "Network outage",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 17, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0));
    }

    @Test
    @DisplayName("should return incident response when incident exists")
    void shouldReturnIncidentWhenFound() {
        // given
        var request = new GetIncidentRequest(1L);
        when(incidentGateway.findById(1L)).thenReturn(Optional.of(sampleIncident()));

        // when
        var result = useCase.execute(request);

        // then
        verify(validator).validate(request);
        verify(incidentGateway).findById(1L);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.onCallPeriodId()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Network outage");
        assertThat(result.startDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 9, 0));
        assertThat(result.endDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 17, 0));
    }

    @Test
    @DisplayName("should throw IncidentNotFoundException when incident does not exist")
    void shouldThrowIncidentNotFoundExceptionWhenMissing() {
        // given
        var request = new GetIncidentRequest(99L);
        when(incidentGateway.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(IncidentNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("should delegate validation to the validator")
    void shouldDelegateToValidator() {
        // given
        var request = new GetIncidentRequest(1L);
        when(incidentGateway.findById(1L)).thenReturn(Optional.of(sampleIncident()));

        // when
        useCase.execute(request);

        // then
        verify(validator).validate(request);
    }
}
