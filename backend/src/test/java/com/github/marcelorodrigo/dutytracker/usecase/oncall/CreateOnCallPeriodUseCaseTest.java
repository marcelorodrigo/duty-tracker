package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.HolidayOverride;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.holiday.PublicHolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CreateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CreateOnCallPeriodValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
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
    PublicHolidayGateway publicHolidayGateway;

    @Mock
    CreateOnCallPeriodValidator validator;

    @InjectMocks
    CreateOnCallPeriodUseCase useCase;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 6, 8, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 13, 8, 0);

    @Test
    @DisplayName("should create period successfully when no holidays fall in range")
    void shouldCreatePeriodSuccessfullyWhenNoHolidaysInRange() {
        // given
        var request = new CreateOnCallPeriodRequest(START, END);
        var saved = new OnCallPeriod(1L, START, END, LocalDateTime.now());
        when(onCallPeriodGateway.save(any())).thenReturn(saved);
        when(publicHolidayGateway.getHolidays(2026)).thenReturn(Set.of());

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isOne();
        assertThat(result.startDateTime()).isEqualTo(START);
        assertThat(result.endDateTime()).isEqualTo(END);
        assertThat(result.holidayOverrides()).isEmpty();
    }

    @Test
    @DisplayName("should seed public holidays that fall within the period")
    void shouldSeedPublicHolidaysThatFallWithinPeriod() {
        // given
        var request = new CreateOnCallPeriodRequest(START, END);
        var saved = new OnCallPeriod(1L, START, END, LocalDateTime.now());
        LocalDate holiday = LocalDate.of(2026, 1, 8);
        when(onCallPeriodGateway.save(any())).thenReturn(saved);
        when(publicHolidayGateway.getHolidays(2026)).thenReturn(Set.of(holiday));
        when(holidayOverrideGateway.save(any())).thenAnswer(inv -> {
            HolidayOverride h = inv.getArgument(0);
            return new HolidayOverride(10L, h.onCallPeriodId(), h.date());
        });

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.holidayOverrides()).containsExactly(holiday);
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
