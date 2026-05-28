package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodOverlapException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CreateOnCallPeriodRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateOnCallPeriodValidatorTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @InjectMocks
    CreateOnCallPeriodValidator validator;

    private static final LocalDateTime START = LocalDateTime.of(2026, 5, 11, 14, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 5, 18, 14, 0);

    @Test
    @DisplayName("should pass validation when period is valid and no overlap exists")
    void shouldPassWhenPeriodIsValidAndNoOverlap() {
        when(onCallPeriodGateway.existsOverlapping(START, END, null)).thenReturn(false);

        assertThatNoException().isThrownBy(() -> validator.validate(new CreateOnCallPeriodRequest(START, END)));
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when end is before start")
    void shouldThrowWhenEndIsBeforeStart() {
        var request = new CreateOnCallPeriodRequest(END, START);
        assertThatExceptionOfType(InvalidOnCallPeriodException.class)
                .isThrownBy(() -> validator.validate(request))
                .withMessage("endDateTime must be after startDateTime");
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when period is less than 1 hour")
    void shouldThrowWhenPeriodIsLessThanOneHour() {
        LocalDateTime almostOneHour = START.plusMinutes(30);

        var request = new CreateOnCallPeriodRequest(START, almostOneHour);
        assertThatExceptionOfType(InvalidOnCallPeriodException.class)
                .isThrownBy(() -> validator.validate(request))
                .withMessage("Period must be at least 1 hour");
    }

    @Test
    @DisplayName("should throw OnCallPeriodOverlapException when period overlaps an existing one")
    void shouldThrowWhenPeriodOverlapsExistingOne() {
        when(onCallPeriodGateway.existsOverlapping(START, END, null)).thenReturn(true);

        var request = new CreateOnCallPeriodRequest(START, END);
        assertThatExceptionOfType(OnCallPeriodOverlapException.class)
                .isThrownBy(() -> validator.validate(request))
                .withMessage("The requested period overlaps with an existing on-call period.");
    }
}
