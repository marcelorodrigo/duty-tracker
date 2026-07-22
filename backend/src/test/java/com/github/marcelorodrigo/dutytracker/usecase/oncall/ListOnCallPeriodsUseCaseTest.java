package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static com.github.marcelorodrigo.dutytracker.TestTime.FIXED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.ListOnCallPeriodsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.ListOnCallPeriodsValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListOnCallPeriodsUseCaseTest {

    @Mock
    OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    HolidayGateway holidayGateway;

    @Mock
    ListOnCallPeriodsValidator validator;

    @InjectMocks
    ListOnCallPeriodsUseCase useCase;

    @Test
    @DisplayName("should return all periods with their holidays")
    void shouldReturnAllPeriodsWithTheirHolidays() {
        // given
        var start1 = LocalDateTime.of(2026, 1, 6, 8, 0);
        var end1 = LocalDateTime.of(2026, 1, 13, 8, 0);
        var start2 = LocalDateTime.of(2026, 1, 20, 8, 0);
        var end2 = LocalDateTime.of(2026, 1, 27, 8, 0);
        var period1 = new OnCallPeriod(1L, start1, end1, FIXED_DATE_TIME);
        var period2 = new OnCallPeriod(2L, start2, end2, FIXED_DATE_TIME);
        var holiday = new Holiday(10L, 1L, LocalDate.of(2026, 1, 8), "New Year");
        when(onCallPeriodGateway.findAll()).thenReturn(List.of(period1, period2));
        when(holidayGateway.findByOnCallPeriodIds(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, List.of(holiday), 2L, List.of()));

        // when
        var result = useCase.execute(new ListOnCallPeriodsRequest());

        // then
        assertThat(result.periods()).hasSize(2);
        assertThat(result.periods().getFirst().id()).isOne();
        assertThat(result.periods().getFirst().holidays()).hasSize(1);
        assertThat(result.periods().getFirst().holidays().getFirst().date()).isEqualTo(LocalDate.of(2026, 1, 8));
        assertThat(result.periods().get(1).id()).isEqualTo(2L);
        assertThat(result.periods().get(1).holidays()).isEmpty();
    }
}
