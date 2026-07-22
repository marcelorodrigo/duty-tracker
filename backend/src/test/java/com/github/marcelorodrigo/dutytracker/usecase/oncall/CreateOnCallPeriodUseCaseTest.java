package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodOverlapException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CreateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CreateOnCallPeriodValidator;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateOnCallPeriodUseCaseTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    CreateOnCallPeriodValidator validator;

    @InjectMocks
    CreateOnCallPeriodUseCase useCase;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 6, 8, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 13, 8, 0);

    @Test
    @DisplayName("should create period successfully and return empty holidays list")
    void shouldCreatePeriodSuccessfullyAndReturnEmptyHolidaysList() {
        // given
        var request = new CreateOnCallPeriodRequest(START, END);
        var saved = new OnCallPeriod(1L, START, END, LocalDateTime.now());
        when(onCallPeriodGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isOne();
        assertThat(result.startDateTime()).isEqualTo(START);
        assertThat(result.endDateTime()).isEqualTo(END);
        assertThat(result.holidays()).isEmpty();
        verify(onCallPeriodGateway).existsOverlapping(START, END, null);
        verify(onCallPeriodGateway).save(any());
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when validator throws")
    void shouldThrowInvalidOnCallPeriodExceptionWhenValidatorThrows() {
        // given
        var request = new CreateOnCallPeriodRequest(END, START);
        doThrow(new InvalidOnCallPeriodException("endDateTime must be after startDateTime"))
                .when(validator)
                .validate(request);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessage("endDateTime must be after startDateTime");
        verify(onCallPeriodGateway, never()).existsOverlapping(any(), any(), any());
    }

    @Test
    @DisplayName("should reject a period when it overlaps an existing period")
    void shouldRejectPeriodWhenItOverlapsExistingPeriod() {
        // given
        var request = new CreateOnCallPeriodRequest(START, END);
        when(onCallPeriodGateway.existsOverlapping(START, END, null)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(OnCallPeriodOverlapException.class)
                .hasMessage("The requested period overlaps with an existing on-call period.");
        verify(onCallPeriodGateway).existsOverlapping(START, END, null);
        verify(onCallPeriodGateway, never()).save(any());
    }
}
