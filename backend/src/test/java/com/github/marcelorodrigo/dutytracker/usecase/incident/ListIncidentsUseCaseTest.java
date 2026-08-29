package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.ListIncidentsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.ListIncidentsValidator;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ListIncidentsUseCaseTest {

    @Mock
    IncidentGateway incidentGateway;

    @Mock
    ListIncidentsValidator validator;

    @InjectMocks
    ListIncidentsUseCase useCase;

    @Test
    @DisplayName("should return paged filtered list when onCallPeriodId is given")
    void shouldReturnFilteredListWhenOnCallPeriodIdIsGiven() {
        // given
        var incident = new Incident(
                1L,
                42L,
                "Test incident",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now());
        var page = new PageImpl<>(List.of(incident), PageRequest.of(0, 20), 1L);
        when(incidentGateway.findByOnCallPeriodId(eq(42L), any(Pageable.class))).thenReturn(page);
        var request = new ListIncidentsRequest(42L, PageRequest.of(0, 20));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().onCallPeriodId()).isEqualTo(42L);
        assertThat(result.page()).isZero();
        assertThat(result.totalElements()).isEqualTo(1L);
        verify(incidentGateway).findByOnCallPeriodId(eq(42L), any(Pageable.class));
    }

    @Test
    @DisplayName("should return paged list of all incidents when onCallPeriodId is null")
    void shouldReturnAllIncidentsWhenOnCallPeriodIdIsNull() {
        // given
        var i1 = new Incident(
                1L, 1L, "Incident one", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now());
        var i2 = new Incident(
                2L, 5L, "Incident two", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now());
        var page = new PageImpl<>(List.of(i1, i2), PageRequest.of(0, 20), 2L);
        when(incidentGateway.findAll(any(Pageable.class))).thenReturn(page);
        var request = new ListIncidentsRequest(null, PageRequest.of(0, 20));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2L);
        verify(incidentGateway).findAll(any(Pageable.class));
    }
}
