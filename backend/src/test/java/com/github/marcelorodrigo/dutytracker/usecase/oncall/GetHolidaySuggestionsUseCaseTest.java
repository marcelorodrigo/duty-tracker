package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHolidaySuggestionRangeException;
import com.github.marcelorodrigo.dutytracker.gateway.holiday.PublicHoliday;
import com.github.marcelorodrigo.dutytracker.gateway.holiday.PublicHolidayGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetHolidaySuggestionsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.GetHolidaySuggestionsValidator;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetHolidaySuggestionsUseCaseTest {

    @Mock
    PublicHolidayGateway publicHolidayGateway;

    @Spy
    GetHolidaySuggestionsValidator validator;

    @InjectMocks
    GetHolidaySuggestionsUseCase useCase;

    private static final LocalDate START = LocalDate.of(2026, 4, 1);
    private static final LocalDate END = LocalDate.of(2026, 4, 30);

    @Test
    @DisplayName("should return holidays mapped to HolidayResponse for the given date range")
    void shouldReturnHolidaysMappedToHolidayResponseForTheGivenDateRange() {
        // given
        var koningsdag = new PublicHoliday(LocalDate.of(2026, 4, 27), "Koningsdag");
        var request = new GetHolidaySuggestionsRequest(START, END);
        when(publicHolidayGateway.getHolidaysWithNames(START, END)).thenReturn(List.of(koningsdag));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().date()).isEqualTo(LocalDate.of(2026, 4, 27));
        assertThat(result.getFirst().name()).isEqualTo("Koningsdag");
    }

    @Test
    @DisplayName("should return empty list when gateway returns no holidays in range")
    void shouldReturnEmptyListWhenGatewayReturnsNoHolidaysInRange() {
        // given
        var request = new GetHolidaySuggestionsRequest(START, END);
        when(publicHolidayGateway.getHolidaysWithNames(START, END)).thenReturn(List.of());

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return multiple holidays when gateway returns several in range")
    void shouldReturnMultipleHolidaysWhenGatewayReturnsSeveralInRange() {
        // given
        var easterMonday = new PublicHoliday(LocalDate.of(2026, 4, 6), "Tweede Paasdag");
        var koningsdag = new PublicHoliday(LocalDate.of(2026, 4, 27), "Koningsdag");
        var request = new GetHolidaySuggestionsRequest(START, END);
        when(publicHolidayGateway.getHolidaysWithNames(START, END)).thenReturn(List.of(easterMonday, koningsdag));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("Tweede Paasdag", "Koningsdag");
    }

    @Test
    @DisplayName("should throw exception when start date is null")
    void shouldThrowExceptionWhenStartDateIsNull() {
        // given
        var request = new GetHolidaySuggestionsRequest(null, END);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(InvalidHolidaySuggestionRangeException.class);
    }

    @Test
    @DisplayName("should throw exception when end date is null")
    void shouldThrowExceptionWhenEndDateIsNull() {
        // given
        var request = new GetHolidaySuggestionsRequest(START, null);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(InvalidHolidaySuggestionRangeException.class);
    }

    @Test
    @DisplayName("should throw exception when start date is after end date")
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        // given
        var request = new GetHolidaySuggestionsRequest(END, START);

        // when / then
        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(InvalidHolidaySuggestionRangeException.class);
    }

    @Test
    @DisplayName("should return holiday when start equals end and a holiday falls on that date")
    void shouldReturnHolidayWhenStartEqualsEndAndHolidayFallsOnThatDate() {
        // given
        var sameDay = LocalDate.of(2026, 4, 27);
        var koningsdag = new PublicHoliday(sameDay, "Koningsdag");
        var request = new GetHolidaySuggestionsRequest(sameDay, sameDay);
        when(publicHolidayGateway.getHolidaysWithNames(sameDay, sameDay)).thenReturn(List.of(koningsdag));

        // when
        var result = useCase.execute(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().date()).isEqualTo(sameDay);
    }
}
