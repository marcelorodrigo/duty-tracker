package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.PaginationRequest;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
    @DisplayName("should return paged periods with their holidays and pagination metadata")
    void shouldReturnPagedPeriodsWithTheirHolidays() {
        // given
        var start1 = LocalDateTime.of(2026, 1, 6, 8, 0);
        var end1 = LocalDateTime.of(2026, 1, 13, 8, 0);
        var start2 = LocalDateTime.of(2026, 1, 20, 8, 0);
        var end2 = LocalDateTime.of(2026, 1, 27, 8, 0);
        var period1 = new OnCallPeriod(1L, start1, end1, LocalDateTime.now());
        var period2 = new OnCallPeriod(2L, start2, end2, LocalDateTime.now());
        var holiday = new Holiday(10L, 1L, LocalDate.of(2026, 1, 8), "New Year");
        var page = new PageImpl<>(List.of(period1, period2), PageRequest.of(0, 20), 2L);
        when(onCallPeriodGateway.findAll(any(PaginationRequest.class))).thenReturn(page);
        when(holidayGateway.findByOnCallPeriodIds(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, List.of(holiday), 2L, List.of()));
        var request = new ListOnCallPeriodsRequest(new PaginationRequest(0, 20, List.of()));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().getFirst().id()).isOne();
        assertThat(result.content().getFirst().holidays()).hasSize(1);
        assertThat(result.content().getFirst().holidays().getFirst().date()).isEqualTo(LocalDate.of(2026, 1, 8));
        assertThat(result.content().get(1).id()).isEqualTo(2L);
        assertThat(result.content().get(1).holidays()).isEmpty();
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(2L);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("should return empty page with metadata and skip holiday fetch when no periods exist")
    void shouldReturnEmptyPageWhenNoPeriodsExist() {
        // given
        when(onCallPeriodGateway.findAll(any(PaginationRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 20), 0L));
        var request = new ListOnCallPeriodsRequest(new PaginationRequest(2, 20, List.of()));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result.content()).isEmpty();
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        verify(holidayGateway, never()).findByOnCallPeriodIds(any());
    }
}
