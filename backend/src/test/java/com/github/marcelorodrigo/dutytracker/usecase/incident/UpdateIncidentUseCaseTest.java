package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.UpdateIncidentValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateIncidentUseCaseTest {

    @Mock
    IncidentGateway incidentGateway;

    @Mock
    UpdateIncidentValidator validator;

    @InjectMocks
    UpdateIncidentUseCase useCase;

    @Test
    @DisplayName("should update incident successfully")
    void shouldUpdateIncidentSuccessfully() {
        // given
        var request =
                new UpdateIncidentRequest(5L, "Updated alert", LocalDate.now(), LocalTime.of(3, 0), LocalTime.of(4, 0));
        var existing = new Incident(
                5L,
                10L,
                "Original alert",
                LocalDate.now().minusDays(1),
                LocalTime.of(1, 0),
                LocalTime.of(2, 0),
                LocalDateTime.now());
        var updated = new Incident(
                5L, 10L, "Updated alert", request.date(), request.startTime(), request.endTime(), existing.createdAt());
        when(incidentGateway.findById(5L)).thenReturn(Optional.of(existing));
        when(incidentGateway.save(any())).thenReturn(updated);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Updated alert");
        assertThat(result.date()).isEqualTo(request.date());
        assertThat(result.startTime()).isEqualTo(request.startTime());
        assertThat(result.endTime()).isEqualTo(request.endTime());
        assertThat(result.onCallPeriodId()).isEqualTo(10L);
        verify(incidentGateway).save(any());
    }

    @Test
    @DisplayName("should throw InvalidIncidentException when incident not found")
    void shouldThrowInvalidIncidentExceptionWhenIncidentNotFound() {
        // given
        var request = new UpdateIncidentRequest(99L, "Test", LocalDate.now(), LocalTime.of(0, 0), LocalTime.of(1, 0));
        doThrow(new InvalidIncidentException("Incident not found"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident not found");
    }
}
