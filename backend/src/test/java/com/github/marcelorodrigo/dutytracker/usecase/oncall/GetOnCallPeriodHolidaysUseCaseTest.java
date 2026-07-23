package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetOnCallPeriodHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.GetOnCallPeriodHolidaysValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetOnCallPeriodHolidaysUseCaseTest {

    @Mock
    private OnCallPeriodGateway onCallPeriodGateway;

    @Mock
    private HolidayGateway holidayGateway;

    @Spy
    private GetOnCallPeriodHolidaysValidator validator;

    @InjectMocks
    private GetOnCallPeriodHolidaysUseCase useCase;

    private static final Long PERIOD_ID = 1L;

    private static OnCallPeriod aOnCallPeriod() {
        return new OnCallPeriod(
                PERIOD_ID,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 14, 23, 59),
                LocalDateTime.of(2024, 1, 1, 10, 0));
    }

    @Test
    @DisplayName("should return list of holidays when period exists and has holidays")
    void shouldReturnListOfHolidaysWhenPeriodExistsAndHasHolidays() {
        // given
        var request = new GetOnCallPeriodHolidaysRequest(PERIOD_ID);
        var holiday = new Holiday(1L, PERIOD_ID, LocalDate.of(2024, 1, 6), "Epiphany");
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(aOnCallPeriod()));
        when(holidayGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(holiday));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().date()).isEqualTo(LocalDate.of(2024, 1, 6));
        assertThat(result.getFirst().name()).isEqualTo("Epiphany");
    }

    @Test
    @DisplayName("should return empty list when period exists but has no holidays")
    void shouldReturnEmptyListWhenPeriodExistsButHasNoHolidays() {
        // given
        var request = new GetOnCallPeriodHolidaysRequest(PERIOD_ID);
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(aOnCallPeriod()));
        when(holidayGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of());

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return multiple holidays when period has several")
    void shouldReturnMultipleHolidaysWhenPeriodHasSeveral() {
        // given
        var request = new GetOnCallPeriodHolidaysRequest(PERIOD_ID);
        var holiday1 = new Holiday(1L, PERIOD_ID, LocalDate.of(2024, 1, 6), "Epiphany");
        var holiday2 = new Holiday(2L, PERIOD_ID, LocalDate.of(2024, 1, 8), "New Year Holiday");
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(aOnCallPeriod()));
        when(holidayGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of(holiday1, holiday2));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("Epiphany", "New Year Holiday");
    }

    @Test
    @DisplayName("should throw on-call period not found exception when period does not exist")
    void shouldThrowOnCallPeriodNotFoundExceptionWhenPeriodDoesNotExist() {
        // given
        var request = new GetOnCallPeriodHolidaysRequest(99L);
        when(onCallPeriodGateway.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(OnCallPeriodNotFoundException.class)
                .hasMessage("On-call period not found: 99");
    }

    @Test
    @DisplayName("should call validator before executing business logic")
    void shouldCallValidatorBeforeExecutingBusinessLogic() {
        // given
        var request = new GetOnCallPeriodHolidaysRequest(PERIOD_ID);
        when(onCallPeriodGateway.findById(PERIOD_ID)).thenReturn(Optional.of(aOnCallPeriod()));
        when(holidayGateway.findByOnCallPeriodId(PERIOD_ID)).thenReturn(List.of());

        // when
        useCase.execute(request);

        // then
        verify(validator).validate(request);
    }
}
