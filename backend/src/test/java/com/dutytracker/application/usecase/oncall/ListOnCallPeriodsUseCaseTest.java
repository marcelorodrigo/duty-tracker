package com.dutytracker.application.usecase.oncall;

import com.dutytracker.domain.gateway.HolidayOverrideGateway;
import com.dutytracker.domain.gateway.OnCallPeriodGateway;
import com.dutytracker.domain.model.HolidayOverride;
import com.dutytracker.domain.model.OnCallPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOnCallPeriodsUseCaseTest {

    @Mock OnCallPeriodGateway onCallPeriodGateway;
    @Mock HolidayOverrideGateway holidayOverrideGateway;
    @Mock ListOnCallPeriodsValidator validator;
    @InjectMocks ListOnCallPeriodsUseCase useCase;

    @Test
    @DisplayName("should return all periods with their holiday overrides")
    void shouldReturnAllPeriodsWithTheirHolidayOverrides() {
        // given
        var start1 = LocalDateTime.of(2026, 1, 6, 8, 0);
        var end1 = LocalDateTime.of(2026, 1, 13, 8, 0);
        var start2 = LocalDateTime.of(2026, 1, 20, 8, 0);
        var end2 = LocalDateTime.of(2026, 1, 27, 8, 0);
        var period1 = new OnCallPeriod(1L, start1, end1, Instant.now());
        var period2 = new OnCallPeriod(2L, start2, end2, Instant.now());
        var override = new HolidayOverride(10L, 1L, LocalDate.of(2026, 1, 8));
        when(onCallPeriodGateway.findAll()).thenReturn(List.of(period1, period2));
        when(holidayOverrideGateway.findByOnCallPeriodId(1L)).thenReturn(List.of(override));
        when(holidayOverrideGateway.findByOnCallPeriodId(2L)).thenReturn(List.of());

        // when
        var result = useCase.execute(new ListOnCallPeriodsRequest());

        // then
        assertThat(result.periods()).hasSize(2);
        assertThat(result.periods().get(0).id()).isEqualTo(1L);
        assertThat(result.periods().get(0).holidayOverrides()).containsExactly(LocalDate.of(2026, 1, 8));
        assertThat(result.periods().get(1).id()).isEqualTo(2L);
        assertThat(result.periods().get(1).holidayOverrides()).isEmpty();
    }
}
