package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentOverlapException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.UpdateIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.UpdateIncidentValidator;
import java.time.LocalDateTime;
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
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    UpdateIncidentValidator validator;

    @InjectMocks
    UpdateIncidentUseCase useCase;

    @Test
    @DisplayName("should update incident successfully")
    void shouldUpdateIncidentSuccessfully() {
        // given
        var request = new UpdateIncidentRequest(
                5L, "Updated alert", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        var existing = new Incident(
                5L,
                10L,
                "Original alert",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1).plusHours(1),
                LocalDateTime.now());
        var updated = new Incident(
                5L, 10L, "Updated alert", request.startDateTime(), request.endDateTime(), existing.createdAt());
        when(incidentGateway.findById(5L)).thenReturn(Optional.of(existing));
        when(onCallPeriodGateway.findById(10L)).thenReturn(Optional.of(periodContaining(request, 10L)));
        when(incidentGateway.save(any())).thenReturn(updated);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Updated alert");
        assertThat(result.startDateTime()).isEqualTo(request.startDateTime());
        assertThat(result.endDateTime()).isEqualTo(request.endDateTime());
        assertThat(result.onCallPeriodId()).isEqualTo(10L);
        verify(incidentGateway).findById(5L);
        verify(onCallPeriodGateway).findById(10L);
        verify(incidentGateway).existsOverlapping(10L, request.startDateTime(), request.endDateTime(), 5L);
        verify(incidentGateway).save(any());
    }

    @Test
    @DisplayName("should throw InvalidIncidentException when incident not found")
    void shouldThrowInvalidIncidentExceptionWhenIncidentNotFound() {
        // given
        var request = new UpdateIncidentRequest(
                99L, "Test", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        when(incidentGateway.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident not found");
        verify(incidentGateway).findById(99L);
        verify(validator, never()).validate(request);
    }

    @Test
    @DisplayName("should reject update where endDateTime equals startDateTime")
    void shouldRejectUpdateWithSameStartAndEndTime() {
        // given
        var now = LocalDateTime.now();
        var request = new UpdateIncidentRequest(5L, "Zero duration incident", now, now);
        when(incidentGateway.findById(5L)).thenReturn(Optional.of(existingIncident(5L, 10L)));
        doThrow(new InvalidIncidentException("Incident endDateTime must be at least 1 minute after startDateTime"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident endDateTime must be at least 1 minute after startDateTime");
    }

    @Test
    @DisplayName("should reject update where endDateTime is before startDateTime")
    void shouldRejectUpdateWithEndBeforeStart() {
        // given
        var start = LocalDateTime.now();
        var end = start.minusMinutes(5);
        var request = new UpdateIncidentRequest(5L, "Invalid time incident", start, end);
        when(incidentGateway.findById(5L)).thenReturn(Optional.of(existingIncident(5L, 10L)));
        doThrow(new InvalidIncidentException("Incident endDateTime must be at least 1 minute after startDateTime"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident endDateTime must be at least 1 minute after startDateTime");
    }

    @Test
    @DisplayName("should accept update where endDateTime is exactly 1 minute after startDateTime")
    void shouldAcceptUpdateWithOneMinuteDuration() {
        // given
        var start = LocalDateTime.now();
        var end = start.plusMinutes(1);
        var request = new UpdateIncidentRequest(5L, "1-minute incident", start, end);
        var existing = new Incident(
                5L,
                10L,
                "Original alert",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1).plusHours(1),
                LocalDateTime.now());
        var updated = new Incident(5L, 10L, "1-minute incident", start, end, existing.createdAt());
        when(incidentGateway.findById(5L)).thenReturn(Optional.of(existing));
        when(onCallPeriodGateway.findById(10L)).thenReturn(Optional.of(periodContaining(request, 10L)));
        when(incidentGateway.save(any())).thenReturn(updated);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("1-minute incident");
        assertThat(result.startDateTime()).isEqualTo(start);
        assertThat(result.endDateTime()).isEqualTo(end);
        verify(validator).validate(request);
        verify(incidentGateway).findById(5L);
        verify(incidentGateway).save(any());
    }

    @Test
    @DisplayName("should reject an update when the incident on-call period is not found")
    void shouldRejectUpdateWhenIncidentOnCallPeriodIsNotFound() {
        // given
        var start = LocalDateTime.of(2026, 7, 22, 9, 0);
        var request = new UpdateIncidentRequest(5L, "Updated alert", start, start.plusHours(1));
        when(incidentGateway.findById(5L)).thenReturn(Optional.of(existingIncident(5L, 10L)));
        when(onCallPeriodGateway.findById(10L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("On-call period not found");
        verify(incidentGateway).findById(5L);
        verify(onCallPeriodGateway).findById(10L);
        verify(incidentGateway, never()).existsOverlapping(any(), any(), any(), any());
    }

    @Test
    @DisplayName("should reject an update overlapping another incident")
    void shouldRejectUpdateOverlappingAnotherIncident() {
        // given
        var start = LocalDateTime.of(2026, 7, 22, 9, 0);
        var request = new UpdateIncidentRequest(5L, "Updated alert", start, start.plusHours(1));
        when(incidentGateway.findById(5L)).thenReturn(Optional.of(existingIncident(5L, 10L)));
        when(onCallPeriodGateway.findById(10L)).thenReturn(Optional.of(periodContaining(request, 10L)));
        when(incidentGateway.existsOverlapping(10L, request.startDateTime(), request.endDateTime(), 5L))
                .thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(IncidentOverlapException.class)
                .hasMessage("Incident overlaps with an existing incident in the same on-call period");
        verify(incidentGateway).findById(5L);
        verify(onCallPeriodGateway).findById(10L);
        verify(incidentGateway).existsOverlapping(10L, request.startDateTime(), request.endDateTime(), 5L);
        verify(incidentGateway, never()).save(any());
    }

    private Incident existingIncident(Long id, Long periodId) {
        var start = LocalDateTime.of(2026, 7, 21, 9, 0);
        return new Incident(id, periodId, "Original alert", start, start.plusHours(1), start.minusDays(1));
    }

    private OnCallPeriod periodContaining(UpdateIncidentRequest request, Long periodId) {
        return new OnCallPeriod(
                periodId,
                request.startDateTime().minusHours(1),
                request.endDateTime().plusHours(1),
                request.startDateTime().minusDays(1));
    }
}
