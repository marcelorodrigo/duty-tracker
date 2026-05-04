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
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.LogIncidentValidator;
import java.time.LocalDateTime;
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
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then - use the mapper to build the expected response
        var expected = mapper.toResponse(saved);
        assertThat(result).isEqualTo(expected);
        verify(incidentGateway).save(any());
        verify(mapper).toDomain(request);
    }

    @Test
    @DisplayName("should create incident when date falls within period")
    void shouldCreateIncidentWhenDateFallsWithinPeriod() {
        // given
        var request = new LogIncidentRequest(
                10L, "DB failure", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        var saved = new Incident(
                2L, 10L, "DB failure", request.startDateTime(), request.endDateTime(), LocalDateTime.now());
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then - assert the whole response using the mapper
        var expected = mapper.toResponse(saved);
        assertThat(result).isEqualTo(expected);
        verify(validator).validate(request);
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
    @DisplayName("Bug #1: should reject incident where endDateTime equals startDateTime")
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
    @DisplayName("Bug #1: should reject incident where endDateTime is before startDateTime")
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
    @DisplayName("Bug #1: should accept incident where endDateTime is exactly 1 minute after startDateTime")
    void shouldAcceptIncidentWithOneMinuteDuration() {
        // given
        var start = LocalDateTime.now();
        var end = start.plusMinutes(1);
        var request = new LogIncidentRequest(10L, "1-minute incident", start, end);
        var saved = new Incident(5L, 10L, "1-minute incident", start, end, LocalDateTime.now());
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then
        var expected = mapper.toResponse(saved);
        assertThat(result).isEqualTo(expected);
        verify(validator).validate(request);
        verify(incidentGateway).save(any());
    }
}
