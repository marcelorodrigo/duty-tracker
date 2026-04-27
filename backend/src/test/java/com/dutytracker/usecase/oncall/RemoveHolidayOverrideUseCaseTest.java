package com.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dutytracker.domain.*;
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
class RemoveHolidayOverrideUseCaseTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    HolidayOverrideGateway holidayOverrideGateway;

    @Mock
    RemoveHolidayOverrideValidator validator;

    @InjectMocks
    RemoveHolidayOverrideUseCase useCase;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 6, 8, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 13, 8, 0);
    private static final LocalDate HOLIDAY = LocalDate.of(2026, 1, 8);

    @Test
    @DisplayName("should remove holiday override and return updated period response")
    void shouldRemoveHolidayOverrideAndReturnUpdatedPeriodResponse() {
        // given
        var request = new RemoveHolidayOverrideRequest(1L, HOLIDAY);
        var period = new OnCallPeriod(1L, START, END, Instant.now());
        var override = new HolidayOverride(10L, 1L, HOLIDAY);
        when(holidayOverrideGateway.findByOnCallPeriodId(1L))
                .thenReturn(List.of(override)) // first call: to find override to delete
                .thenReturn(List.of()); // second call: reload remaining
        when(onCallPeriodGateway.findById(1L)).thenReturn(Optional.of(period));

        // when
        var result = useCase.execute(request);

        // then
        verify(holidayOverrideGateway).deleteById(10L);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.holidayOverrides()).isEmpty();
    }
}
