package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.configuration.BusinessClock;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CreateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CreateOnCallPeriodValidator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateOnCallPeriodUseCaseTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    CreateOnCallPeriodValidator validator;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 6, 8, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 13, 8, 0);

    // 2026-01-06T07:00Z == 2026-01-06T08:00 Europe/Amsterdam (CET, UTC+1 in January)
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-01-06T07:00:00Z"), BusinessClock.BUSINESS_ZONE);

    private CreateOnCallPeriodUseCase useCase;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        useCase = new CreateOnCallPeriodUseCase(onCallPeriodGateway, validator, FIXED_CLOCK);
    }

    @Test
    @DisplayName("should create period successfully and return empty holidays list")
    void shouldCreatePeriodSuccessfullyAndReturnEmptyHolidaysList() {
        // given
        var request = new CreateOnCallPeriodRequest(START, END);
        var saved = new OnCallPeriod(1L, START, END, LocalDateTime.now(FIXED_CLOCK));
        when(onCallPeriodGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isOne();
        assertThat(result.startDateTime()).isEqualTo(START);
        assertThat(result.endDateTime()).isEqualTo(END);
        assertThat(result.holidays()).isEmpty();
    }

    @Test
    @DisplayName("should stamp createdAt from the injected business clock")
    void shouldStampCreatedAtFromInjectedBusinessClock() {
        // given
        var request = new CreateOnCallPeriodRequest(START, END);
        when(onCallPeriodGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.createdAt()).isEqualTo(LocalDateTime.of(2026, 1, 6, 8, 0));
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
    }
}
