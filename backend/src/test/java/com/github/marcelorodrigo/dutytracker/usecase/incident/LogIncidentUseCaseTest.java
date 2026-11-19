package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.configuration.BusinessClock;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.LogIncidentValidator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    // 2026-01-06T07:00Z == 2026-01-06T08:00 Europe/Amsterdam (CET, UTC+1 in January)
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-01-06T07:00:00Z"), BusinessClock.BUSINESS_ZONE);

    private static final LocalDateTime EXPECTED_CREATED_AT = LocalDateTime.of(2026, 1, 6, 8, 0);

    private LogIncidentUseCase useCase;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        useCase = new LogIncidentUseCase(incidentGateway, validator, mapper, FIXED_CLOCK);
    }

    @Test
    @DisplayName("should create incident successfully")
    void shouldCreateIncidentSuccessfully() {
        // given
        var request = new LogIncidentRequest(
                10L, "Network outage", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        var saved = new Incident(
                1L, 10L, "Network outage", request.startDateTime(), request.endDateTime(), EXPECTED_CREATED_AT);
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then - use the mapper to build the expected response
        var expected = mapper.toResponse(saved);
        assertThat(result).isEqualTo(expected);
        verify(incidentGateway).save(any());
        verify(mapper).toDomain(request, EXPECTED_CREATED_AT);
    }

    @Test
    @DisplayName("should stamp createdAt from the injected business clock")
    void shouldStampCreatedAtFromInjectedBusinessClock() {
        // given
        var request = new LogIncidentRequest(
                10L, "Network outage", LocalDateTime.of(2026, 1, 6, 9, 0), LocalDateTime.of(2026, 1, 6, 10, 0));
        when(incidentGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        useCase.execute(request);

        // then
        var captor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentGateway).save(captor.capture());
        assertThat(captor.getValue().createdAt()).isEqualTo(EXPECTED_CREATED_AT);
    }

    @Test
    @DisplayName("should create incident when date falls within period")
    void shouldCreateIncidentWhenDateFallsWithinPeriod() {
        // given
        var request = new LogIncidentRequest(
                10L, "DB failure", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        var saved = new Incident(
                2L, 10L, "DB failure", request.startDateTime(), request.endDateTime(), EXPECTED_CREATED_AT);
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
        var saved = new Incident(5L, 10L, "1-minute incident", start, end, EXPECTED_CREATED_AT);
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
