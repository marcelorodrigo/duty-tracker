package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodOverlapException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateOnCallPeriodRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateOnCallPeriodValidatorTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @InjectMocks
    UpdateOnCallPeriodValidator validator;

    private static final Long PERIOD_ID = 42L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 5, 11, 14, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 5, 18, 14, 0);

    @Test
    @DisplayName("should pass validation when period is valid and no other period overlaps (or overlaps only itself)")
    void shouldPassWhenPeriodIsValidAndNoOverlap() {
        when(onCallPeriodGateway.existsOverlapping(START, END, PERIOD_ID)).thenReturn(false);

        var request = new UpdateOnCallPeriodRequest(PERIOD_ID, START, END);
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when end is before start")
    void shouldThrowWhenEndIsBeforeStart() {
        assertThatExceptionOfType(InvalidOnCallPeriodException.class)
                .isThrownBy(() -> validator.validate(new UpdateOnCallPeriodRequest(PERIOD_ID, END, START)))
                .withMessage("endDateTime must be after startDateTime");
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when period is less than 1 hour")
    void shouldThrowWhenPeriodIsLessThanOneHour() {
        LocalDateTime almostOneHour = START.plusMinutes(30);

        var request = new UpdateOnCallPeriodRequest(PERIOD_ID, START, almostOneHour);
        assertThatExceptionOfType(InvalidOnCallPeriodException.class)
                .isThrownBy(() -> validator.validate(request))
                .withMessage("Period must be at least 1 hour");
    }

    @Test
    @DisplayName("should throw OnCallPeriodOverlapException when period overlaps a different existing period")
    void shouldThrowWhenPeriodOverlapsDifferentExistingOne() {
        when(onCallPeriodGateway.existsOverlapping(START, END, PERIOD_ID)).thenReturn(true);

        var request = new UpdateOnCallPeriodRequest(PERIOD_ID, START, END);
        assertThatExceptionOfType(OnCallPeriodOverlapException.class)
                .isThrownBy(() -> validator.validate(request))
                .withMessage("The requested period overlaps with an existing on-call period.");
    }
}
