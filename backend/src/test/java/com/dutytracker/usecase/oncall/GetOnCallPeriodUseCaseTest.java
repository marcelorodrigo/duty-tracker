package com.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.validator.oncall.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetOnCallPeriodUseCaseTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    HolidayOverrideGateway holidayOverrideGateway;

    @Mock
    GetOnCallPeriodValidator validator;

    @InjectMocks
    GetOnCallPeriodUseCase useCase;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 6, 8, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 13, 8, 0);

    @Test
    @DisplayName("should return period with holiday overrides when period is found")
    void shouldReturnPeriodWithHolidayOverridesWhenPeriodIsFound() {
        // given
        var period = new OnCallPeriod(1L, START, END, Instant.now());
        var override = new HolidayOverride(10L, 1L, LocalDate.of(2026, 1, 8));
        when(onCallPeriodGateway.findById(1L)).thenReturn(Optional.of(period));
        when(holidayOverrideGateway.findByOnCallPeriodId(1L)).thenReturn(List.of(override));
        var request = new GetOnCallPeriodRequest(1L);

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.holidayOverrides()).containsExactly(LocalDate.of(2026, 1, 8));
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when period is not found")
    void shouldThrowInvalidOnCallPeriodExceptionWhenPeriodIsNotFound() {
        // given
        when(onCallPeriodGateway.findById(99L)).thenReturn(Optional.empty());
        var request = new GetOnCallPeriodRequest(99L);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessage("Period not found");
    }
}
