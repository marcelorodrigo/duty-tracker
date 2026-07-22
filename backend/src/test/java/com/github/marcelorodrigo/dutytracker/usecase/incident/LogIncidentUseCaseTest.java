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
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.LogIncidentValidator;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogIncidentUseCaseTest {

    @Mock
    IncidentGateway incidentGateway;

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    LogIncidentValidator validator;

    @Spy
    IncidentResponseMapper mapper = new IncidentResponseMapperImpl();

    @InjectMocks
    LogIncidentUseCase useCase;

    @Test
    @DisplayName("should create incident successfully")
    void shouldCreateIncidentSuccessfully() {
        // given
        var request = new LogIncidentRequest(
                10L, "Network outage", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        var saved = new Incident(
                1L, 10L, "Network outage", request.startDateTime(), request.endDateTime(), LocalDateTime.now());
        when(onCallPeriodGateway.findById(10L)).thenReturn(Optional.of(periodContaining(request)));
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then - use the mapper to build the expected response
        var expected = mapper.toResponse(saved);
        assertThat(result).isEqualTo(expected);
        verify(incidentGateway).save(any());
        verify(mapper).toDomain(request);
        verify(onCallPeriodGateway).findById(10L);
        verify(incidentGateway).existsOverlapping(10L, request.startDateTime(), request.endDateTime(), null);
    }

    @Test
    @DisplayName("should create incident when date falls within period")
    void shouldCreateIncidentWhenDateFallsWithinPeriod() {
        // given
        var request = new LogIncidentRequest(
                10L, "DB failure", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        var saved = new Incident(
                2L, 10L, "DB failure", request.startDateTime(), request.endDateTime(), LocalDateTime.now());
        when(onCallPeriodGateway.findById(10L)).thenReturn(Optional.of(periodContaining(request)));
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then - assert the whole response using the mapper
        var expected = mapper.toResponse(saved);
        assertThat(result).isEqualTo(expected);
        verify(validator).validate(request);
        verify(onCallPeriodGateway).findById(10L);
    }

    @Test
    @DisplayName("should throw InvalidIncidentException when validator throws")
    void shouldThrowInvalidIncidentExceptionWhenValidatorThrows() {
        // given
        var request = new LogIncidentRequest(
                99L,
                "Test",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1));
        doThrow(new InvalidIncidentException("Incident date cannot be in the future"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident date cannot be in the future");
    }

    @Test
    @DisplayName("should reject incident where endDateTime equals startDateTime")
    void shouldRejectIncidentWithSameStartAndEndTime() {
        // given
        var now = LocalDateTime.now();
        var request = new LogIncidentRequest(10L, "Zero duration incident", now, now);
        doThrow(new InvalidIncidentException("Incident endDateTime must be at least 1 minute after startDateTime"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident endDateTime must be at least 1 minute after startDateTime");
    }

    @Test
    @DisplayName("should reject incident where endDateTime is before startDateTime")
    void shouldRejectIncidentWithEndBeforeStart() {
        // given
        var start = LocalDateTime.now();
        var end = start.minusMinutes(5);
        var request = new LogIncidentRequest(10L, "Invalid time incident", start, end);
        doThrow(new InvalidIncidentException("Incident endDateTime must be at least 1 minute after startDateTime"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident endDateTime must be at least 1 minute after startDateTime");
    }

    @Test
    @DisplayName("should accept incident where endDateTime is exactly 1 minute after startDateTime")
    void shouldAcceptIncidentWithOneMinuteDuration() {
        // given
        var start = LocalDateTime.now();
        var end = start.plusMinutes(1);
        var request = new LogIncidentRequest(10L, "1-minute incident", start, end);
        var saved = new Incident(5L, 10L, "1-minute incident", start, end, LocalDateTime.now());
        when(onCallPeriodGateway.findById(10L)).thenReturn(Optional.of(periodContaining(request)));
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then
        var expected = mapper.toResponse(saved);
        assertThat(result).isEqualTo(expected);
        verify(validator).validate(request);
        verify(incidentGateway).save(any());
    }

    @Test
    @DisplayName("should reject an incident when its on-call period is not found")
    void shouldRejectIncidentWhenOnCallPeriodIsNotFound() {
        // given
        var start = LocalDateTime.of(2026, 7, 22, 9, 0);
        var request = new LogIncidentRequest(99L, "Database outage", start, start.plusHours(1));
        when(onCallPeriodGateway.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("Period not found");
        verify(onCallPeriodGateway).findById(99L);
        verify(incidentGateway, never()).existsOverlapping(any(), any(), any(), any());
        verify(incidentGateway, never()).save(any());
    }

    @Test
    @DisplayName("should reject an incident outside its on-call period")
    void shouldRejectIncidentOutsideItsOnCallPeriod() {
        // given
        var periodStart = LocalDateTime.of(2026, 7, 22, 9, 0);
        var period = new OnCallPeriod(10L, periodStart, periodStart.plusHours(8), periodStart);
        var request =
                new LogIncidentRequest(10L, "Early alert", periodStart.minusMinutes(1), periodStart.plusMinutes(30));
        when(onCallPeriodGateway.findById(10L)).thenReturn(Optional.of(period));

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessage("Incident startDateTime must be within the on-call period");
        verify(onCallPeriodGateway).findById(10L);
        verify(incidentGateway, never()).existsOverlapping(any(), any(), any(), any());
    }

    @Test
    @DisplayName("should reject an incident overlapping another incident")
    void shouldRejectIncidentOverlappingAnotherIncident() {
        // given
        var start = LocalDateTime.of(2026, 7, 22, 9, 0);
        var request = new LogIncidentRequest(10L, "Database outage", start, start.plusHours(1));
        when(onCallPeriodGateway.findById(10L)).thenReturn(Optional.of(periodContaining(request)));
        when(incidentGateway.existsOverlapping(10L, request.startDateTime(), request.endDateTime(), null))
                .thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(IncidentOverlapException.class)
                .hasMessage("Incident overlaps with an existing incident in the same on-call period");
        verify(onCallPeriodGateway).findById(10L);
        verify(incidentGateway).existsOverlapping(10L, request.startDateTime(), request.endDateTime(), null);
        verify(incidentGateway, never()).save(any());
    }

    private OnCallPeriod periodContaining(LogIncidentRequest request) {
        return new OnCallPeriod(
                request.onCallPeriodId(),
                request.startDateTime().minusHours(1),
                request.endDateTime().plusHours(1),
                request.startDateTime().minusDays(1));
    }
}
