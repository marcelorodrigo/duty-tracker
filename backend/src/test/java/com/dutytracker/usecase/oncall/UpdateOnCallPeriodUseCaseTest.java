package com.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.validator.oncall.*;
import java.time.Instant;
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
class UpdateOnCallPeriodUseCaseTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    HolidayOverrideGateway holidayOverrideGateway;

    @Mock
    UpdateOnCallPeriodValidator validator;

    @InjectMocks
    UpdateOnCallPeriodUseCase useCase;

    private static final LocalDateTime OLD_START = LocalDateTime.of(2026, 1, 6, 8, 0);
    private static final LocalDateTime OLD_END = LocalDateTime.of(2026, 1, 13, 8, 0);
    private static final LocalDateTime NEW_START = LocalDateTime.of(2026, 1, 6, 9, 0);
    private static final LocalDateTime NEW_END = LocalDateTime.of(2026, 1, 13, 9, 0);

    @Test
    @DisplayName("should update period and return updated response")
    void shouldUpdatePeriodAndReturnUpdatedResponse() {
        // given
        var existing = new OnCallPeriod(1L, OLD_START, OLD_END, Instant.now());
        var updated = new OnCallPeriod(1L, NEW_START, NEW_END, existing.createdAt());
        var request = new UpdateOnCallPeriodRequest(1L, NEW_START, NEW_END);
        when(onCallPeriodGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(onCallPeriodGateway.save(any())).thenReturn(updated);
        when(holidayOverrideGateway.findByOnCallPeriodId(1L)).thenReturn(List.of());

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.startDateTime()).isEqualTo(NEW_START);
        assertThat(result.endDateTime()).isEqualTo(NEW_END);
    }

    @Test
    @DisplayName("should throw InvalidOnCallPeriodException when period is not found")
    void shouldThrowInvalidOnCallPeriodExceptionWhenPeriodIsNotFound() {
        // given
        var request = new UpdateOnCallPeriodRequest(99L, NEW_START, NEW_END);
        when(onCallPeriodGateway.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessage("Period not found");
    }
}
