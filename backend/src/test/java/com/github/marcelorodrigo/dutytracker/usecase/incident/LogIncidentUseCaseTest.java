package com.github.marcelorodrigo.dutytracker.usecase.incident;

import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_CLOCK;
import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.LogIncidentRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.LogIncidentValidator;
import org.junit.jupiter.api.BeforeEach;
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
    private IncidentGateway incidentGateway;

    @Mock
    private LogIncidentValidator validator;

    @Spy
    private IncidentResponseMapper mapper = new IncidentResponseMapperImpl();

    private LogIncidentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LogIncidentUseCase(incidentGateway, validator, mapper, FIXED_CLOCK);
    }

    @Test
    @DisplayName("should create incident successfully")
    void shouldCreateIncidentSuccessfully() {
        // given
        var request = new LogIncidentRequest(10L, "Network outage", FIXED_DATE_TIME, FIXED_DATE_TIME.plusHours(1));
        var saved = new Incident(
                1L, 10L, "Network outage", request.startDateTime(), request.endDateTime(), FIXED_DATE_TIME);
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then
        var expected = mapper.toResponse(saved);
        assertThat(result).isEqualTo(expected);
        var incidentCaptor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentGateway).save(incidentCaptor.capture());
        assertThat(incidentCaptor.getValue().createdAt()).isEqualTo(FIXED_DATE_TIME);
        verify(mapper).toDomain(request, FIXED_CLOCK);
    }

    @Test
    @DisplayName("should create incident when date falls within period")
    void shouldCreateIncidentWhenDateFallsWithinPeriod() {
        // given
        var request = new LogIncidentRequest(10L, "DB failure", FIXED_DATE_TIME, FIXED_DATE_TIME.plusHours(1));
        var saved =
                new Incident(2L, 10L, "DB failure", request.startDateTime(), request.endDateTime(), FIXED_DATE_TIME);
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then
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
                FIXED_DATE_TIME.plusDays(1),
                FIXED_DATE_TIME.plusDays(1).plusHours(1));
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
        var now = FIXED_DATE_TIME;
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
        var start = FIXED_DATE_TIME;
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
        var start = FIXED_DATE_TIME;
        var end = start.plusMinutes(1);
        var request = new LogIncidentRequest(10L, "1-minute incident", start, end);
        var saved = new Incident(5L, 10L, "1-minute incident", start, end, FIXED_DATE_TIME);
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
