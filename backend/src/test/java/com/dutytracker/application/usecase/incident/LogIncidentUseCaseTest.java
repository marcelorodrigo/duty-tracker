package com.dutytracker.application.usecase.incident;

import com.dutytracker.domain.exception.InvalidIncidentException;
import com.dutytracker.domain.exception.OnboardingNotCompletedException;
import com.dutytracker.domain.gateway.IncidentGateway;
import com.dutytracker.domain.model.Incident;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogIncidentUseCaseTest {

    @Mock
    IncidentGateway incidentGateway;

    @Mock
    LogIncidentValidator validator;

    @InjectMocks
    LogIncidentUseCase useCase;

    @Test
    @DisplayName("should create incident when onCallPeriodId is null")
    void shouldCreateIncidentWhenOnCallPeriodIdIsNull() {
        // given
        var request = new LogIncidentRequest(null, LocalDate.now(), LocalTime.of(2, 0), LocalTime.of(3, 0));
        var saved = new Incident(1L, null, request.date(), request.startTime(), request.endTime(), Instant.now());
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.onCallPeriodId()).isNull();
        verify(incidentGateway).save(any());
    }

    @Test
    @DisplayName("should create incident when date falls within period")
    void shouldCreateIncidentWhenDateFallsWithinPeriod() {
        // given
        var request = new LogIncidentRequest(10L, LocalDate.now(), LocalTime.of(1, 0), LocalTime.of(2, 0));
        var saved = new Incident(2L, 10L, request.date(), request.startTime(), request.endTime(), Instant.now());
        when(incidentGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.onCallPeriodId()).isEqualTo(10L);
        verify(validator).validate(request);
    }

    @Test
    @DisplayName("should throw InvalidIncidentException when validator throws")
    void shouldThrowInvalidIncidentExceptionWhenValidatorThrows() {
        // given
        var request = new LogIncidentRequest(99L, LocalDate.now().plusDays(1), LocalTime.of(0, 0), LocalTime.of(1, 0));
        doThrow(new InvalidIncidentException("Incident date cannot be in the future"))
                .when(validator).validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidIncidentException.class)
                .hasMessageContaining("Incident date cannot be in the future");
    }

    @Test
    @DisplayName("should throw OnboardingNotCompletedException when onboarding is incomplete")
    void shouldThrowOnboardingNotCompletedExceptionWhenOnboardingIncomplete() {
        // given
        var request = new LogIncidentRequest(null, LocalDate.now(), LocalTime.of(0, 0), LocalTime.of(1, 0));
        doThrow(new OnboardingNotCompletedException("Onboarding must be completed before logging incidents"))
                .when(validator).validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(OnboardingNotCompletedException.class)
                .hasMessageContaining("Onboarding must be completed");
    }
}
