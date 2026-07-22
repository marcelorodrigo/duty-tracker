package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_CLOCK;
import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CreateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CreateOnCallPeriodValidator;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateOnCallPeriodUseCaseTest {

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private CreateOnCallPeriodValidator validator;

    private CreateOnCallPeriodUseCase useCase;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 6, 8, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 13, 8, 0);

    @BeforeEach
    void setUp() {
        useCase = new CreateOnCallPeriodUseCase(onCallPeriodGateway, validator, FIXED_CLOCK);
    }

    @Test
    @DisplayName("should create period successfully and return empty holidays list")
    void shouldCreatePeriodSuccessfullyAndReturnEmptyHolidaysList() {
        // given
        var request = new CreateOnCallPeriodRequest(START, END);
        var saved = new OnCallPeriod(1L, START, END, FIXED_DATE_TIME);
        when(onCallPeriodGateway.save(any())).thenReturn(saved);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isOne();
        assertThat(result.startDateTime()).isEqualTo(START);
        assertThat(result.endDateTime()).isEqualTo(END);
        assertThat(result.createdAt()).isEqualTo(FIXED_DATE_TIME);
        assertThat(result.holidays()).isEmpty();
        var periodCaptor = ArgumentCaptor.forClass(OnCallPeriod.class);
        verify(onCallPeriodGateway).save(periodCaptor.capture());
        assertThat(periodCaptor.getValue().createdAt()).isEqualTo(FIXED_DATE_TIME);
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
