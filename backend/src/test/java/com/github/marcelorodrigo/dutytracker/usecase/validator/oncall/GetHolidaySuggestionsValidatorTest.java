package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHolidaySuggestionRangeException;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetHolidaySuggestionsRequest;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetHolidaySuggestionsValidatorTest {

    private final GetHolidaySuggestionsValidator validator = new GetHolidaySuggestionsValidator();

    @Test
    @DisplayName("should accept a valid holiday suggestion range")
    void shouldAcceptAValidHolidaySuggestionRange() {
        // given
        var request = new GetHolidaySuggestionsRequest(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should reject a range with a missing start date")
    void shouldRejectRangeWithMissingStartDate() {
        // given
        var request = new GetHolidaySuggestionsRequest(null, LocalDate.of(2025, 12, 31));

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isExactlyInstanceOf(InvalidHolidaySuggestionRangeException.class)
                .hasMessage("Start and end dates are required");
    }

    @Test
    @DisplayName("should reject a range with a missing end date")
    void shouldRejectRangeWithMissingEndDate() {
        // given
        var request = new GetHolidaySuggestionsRequest(LocalDate.of(2025, 1, 1), null);

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isExactlyInstanceOf(InvalidHolidaySuggestionRangeException.class)
                .hasMessage("Start and end dates are required");
    }

    @Test
    @DisplayName("should reject a range whose start date is after its end date")
    void shouldRejectRangeWhenStartDateIsAfterEndDate() {
        // given
        var request = new GetHolidaySuggestionsRequest(LocalDate.of(2025, 12, 31), LocalDate.of(2025, 1, 1));

        // when / then
        assertThatThrownBy(() -> validator.validate(request))
                .isExactlyInstanceOf(InvalidHolidaySuggestionRangeException.class)
                .hasMessage("Start date must not be after end date");
    }
}
