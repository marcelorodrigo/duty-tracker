package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateHolidaysUseCaseTest {

    private static final long PERIOD_ID = 7L;
    private static final OnCallPeriod PERIOD = new OnCallPeriod(
            PERIOD_ID,
            LocalDateTime.of(2026, 12, 24, 0, 0),
            LocalDateTime.of(2026, 12, 27, 0, 0),
            LocalDateTime.of(2026, 7, 22, 12, 0));

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private HolidayGateway holidayGateway;

    @Captor
    private ArgumentCaptor<List<Holiday>> holidaysCaptor;

    @InjectMocks
    private UpdateHolidaysUseCase useCase;

    @Test
    @DisplayName("should replace holidays and return the persisted values")
    void shouldReplaceHolidaysAndReturnPersistedValues() {
        // given
        var christmas = new HolidayResponse(LocalDate.of(2026, 12, 25), "Christmas");
        var boxingDay = new HolidayResponse(LocalDate.of(2026, 12, 26), "Boxing Day");
        var request = new UpdateHolidaysRequest(PERIOD_ID, List.of(christmas, boxingDay));
        var saved = List.of(
                new Holiday(10L, PERIOD_ID, christmas.date(), christmas.name()),
                new Holiday(11L, PERIOD_ID, boxingDay.date(), boxingDay.name()));
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(holidayGateway.saveAll(anyList())).thenReturn(saved);
        // when
        var result = useCase.execute(request);

        // then
        assertThat(result)
                .extracting(HolidayResponse::date, HolidayResponse::name)
                .containsExactly(
                        tuple(LocalDate.of(2026, 12, 25), "Christmas"),
                        tuple(LocalDate.of(2026, 12, 26), "Boxing Day"));
        verify(holidayGateway).deleteByOnCallPeriodId(PERIOD_ID);
        verify(holidayGateway).saveAll(holidaysCaptor.capture());
        assertThat(holidaysCaptor.getValue())
                .extracting(Holiday::id, Holiday::onCallPeriodId, Holiday::date, Holiday::name)
                .containsExactly(
                        tuple(null, PERIOD_ID, LocalDate.of(2026, 12, 25), "Christmas"),
                        tuple(null, PERIOD_ID, LocalDate.of(2026, 12, 26), "Boxing Day"));
    }

    @Test
    @DisplayName("should clear holidays when the replacement is empty")
    void shouldClearHolidaysWhenReplacementIsEmpty() {
        // given
        var request = new UpdateHolidaysRequest(PERIOD_ID, List.of());
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(holidayGateway.saveAll(List.of())).thenReturn(List.of());

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result).isEmpty();
        verify(holidayGateway).deleteByOnCallPeriodId(PERIOD_ID);
        verify(holidayGateway).saveAll(List.of());
    }

    @Test
    @DisplayName("should reject the replacement when the period does not exist")
    void shouldRejectReplacementWhenPeriodDoesNotExist() {
        // given
        var request = new UpdateHolidaysRequest(
                PERIOD_ID, List.of(new HolidayResponse(LocalDate.of(2026, 12, 25), "Christmas")));
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidOnCallPeriodException.class)
                .hasMessage("Period not found");
        verify(holidayGateway, never()).deleteByOnCallPeriodId(PERIOD_ID);
        verify(holidayGateway, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("should propagate a replacement persistence failure")
    void shouldPropagateReplacementPersistenceFailure() {
        // given
        var request = new UpdateHolidaysRequest(
                PERIOD_ID, List.of(new HolidayResponse(LocalDate.of(2026, 12, 25), "Christmas")));
        var failure = new IllegalStateException("Persistence unavailable");
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(PERIOD));
        when(holidayGateway.saveAll(anyList())).thenThrow(failure);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request)).isSameAs(failure);
        verify(holidayGateway).deleteByOnCallPeriodId(PERIOD_ID);
    }
}
