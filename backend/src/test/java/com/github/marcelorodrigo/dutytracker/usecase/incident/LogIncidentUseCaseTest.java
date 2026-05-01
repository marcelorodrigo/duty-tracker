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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @DisplayName("should create incident when onCallPeriodId is null")
    void shouldCreateIncidentWhenOnCallPeriodIdIsNull() {
        // given
        var request =
                new LogIncidentRequest(null, "Network outage", LocalDate.now(), LocalTime.of(2, 0), LocalTime.of(3, 0));
        var saved = new Incident(
                1L,
                null,
                "Network outage",
                request.date(),
                request.startTime(),
                request.endTime(),
                LocalDateTime.now());
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
        var request =
                new LogIncidentRequest(10L, "DB failure", LocalDate.now(), LocalTime.of(1, 0), LocalTime.of(2, 0));
        var saved = new Incident(
                2L, 10L, "DB failure", request.date(), request.startTime(), request.endTime(), LocalDateTime.now());
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
                99L, "Test", LocalDate.now().plusDays(1), LocalTime.of(0, 0), LocalTime.of(1, 0));
        doThrow(new InvalidIncidentException("Incident date cannot be in the future"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident date cannot be in the future");
    }
}
