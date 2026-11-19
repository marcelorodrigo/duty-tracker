package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.UpdateHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateHolidaysValidatorTest {

    private final UpdateHolidaysValidator validator = new UpdateHolidaysValidator();

    @Test
    @DisplayName("should accept an update holidays request")
    void shouldAcceptUpdateHolidaysRequest() {
        // given
        var request =
                new UpdateHolidaysRequest(1L, List.of(new HolidayResponse(LocalDate.of(2025, 1, 1), "New Year's Day")));

        // when / then
        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}
