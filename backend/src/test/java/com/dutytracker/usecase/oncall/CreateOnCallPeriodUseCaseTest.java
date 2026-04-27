package com.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.gateway.preferences.UserPreferencesGateway;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.validator.oncall.*;
import java.time.Instant;
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
    HolidayOverrideGateway holidayOverrideGateway;

    @Mock
    UserPreferencesGateway userPreferencesGateway;

    @Mock
    CreateOnCallPeriodValidator validator;

    @InjectMocks
    CreateOnCallPeriodUseCase useCase;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 6, 8, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 13, 8, 0);

    @Test
    @DisplayName("should create period successfully when request is valid")
    void shouldCreatePeriodSuccessfullyWhenRequestIsValid() {
        // given
        var request = new CreateOnCallPeriodRequest(START, END);
        var saved = new OnCallPeriod(1L, START, END, Instant.now());
        when(onCallPeriodGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.startDateTime()).isEqualTo(START);
        assertThat(result.endDateTime()).isEqualTo(END);
        assertThat(result.holidayOverrides()).isEmpty();
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
