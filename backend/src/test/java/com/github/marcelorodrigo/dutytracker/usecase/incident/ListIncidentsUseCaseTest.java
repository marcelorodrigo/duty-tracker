package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.ListIncidentsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.ListIncidentsValidator;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListIncidentsUseCaseTest {

    @Mock
    IncidentGateway incidentGateway;

    @Mock
    ListIncidentsValidator validator;

    @InjectMocks
    ListIncidentsUseCase useCase;

    @Test
    @DisplayName("should return filtered list when onCallPeriodId is given")
    void shouldReturnFilteredListWhenOnCallPeriodIdIsGiven() {
        // given
        var incident = new Incident(
                1L,
                42L,
                "Test incident",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now());
        when(incidentGateway.findByOnCallPeriodId(42L)).thenReturn(List.of(incident));

        // when
        var result = useCase.execute(new ListIncidentsRequest(42L));

        // then
        assertThat(result.incidents()).hasSize(1);
        assertThat(result.incidents().getFirst().onCallPeriodId()).isEqualTo(42L);
        verify(incidentGateway).findByOnCallPeriodId(42L);
    }

    @Test
    @DisplayName("should return all incidents when onCallPeriodId is null")
    void shouldReturnAllIncidentsWhenOnCallPeriodIdIsNull() {
        // given
        var i1 = new Incident(
                1L, 1L, "Incident one", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now());
        var i2 = new Incident(
                2L, 5L, "Incident two", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now());
        when(incidentGateway.findAll()).thenReturn(List.of(i1, i2));

        // when
        var result = useCase.execute(new ListIncidentsRequest(null));

        // then
        assertThat(result.incidents()).hasSize(2);
        verify(incidentGateway).findAll();
    }
}
