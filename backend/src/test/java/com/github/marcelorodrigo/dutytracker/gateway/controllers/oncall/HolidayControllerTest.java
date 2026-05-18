package com.github.marcelorodrigo.dutytracker.gateway.controllers.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidHolidaySuggestionRangeException;
import com.github.marcelorodrigo.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.GetHolidaySuggestionsUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetHolidaySuggestionsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(HolidayController.class)
@Import(GlobalExceptionHandler.class)
class HolidayControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private GetHolidaySuggestionsUseCase getSuggestions;

    @Test
    @DisplayName("GET /api/v1/holidays/suggestions returns 200 with holiday list")
    void shouldReturnHolidaySuggestions() {
        var holiday = new HolidayResponse(LocalDate.of(2024, 12, 25), "Christmas Day");
        given(getSuggestions.execute(any(GetHolidaySuggestionsRequest.class)))
                .willReturn(List.of(holiday));

        assertThat(mvc.get()
                        .uri("/api/v1/holidays/suggestions")
                        .param("start", "2024-12-01")
                        .param("end", "2024-12-31"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("Christmas Day");
    }

    @Test
    @DisplayName("GET /api/v1/holidays/suggestions returns 200 with multiple holidays")
    void shouldReturnMultipleHolidays() {
        var holidays = List.of(
                new HolidayResponse(LocalDate.of(2024, 12, 25), "Christmas Day"),
                new HolidayResponse(LocalDate.of(2024, 12, 26), "Boxing Day"));
        given(getSuggestions.execute(any(GetHolidaySuggestionsRequest.class)))
                .willReturn(holidays);

        assertThat(mvc.get()
                        .uri("/api/v1/holidays/suggestions")
                        .param("start", "2024-12-01")
                        .param("end", "2024-12-31"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.length()")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("GET /api/v1/holidays/suggestions returns 200 with empty list when no holidays")
    void shouldReturnEmptyListWhenNoHolidays() {
        given(getSuggestions.execute(any(GetHolidaySuggestionsRequest.class)))
                .willReturn(List.of());

        assertThat(mvc.get()
                        .uri("/api/v1/holidays/suggestions")
                        .param("start", "2024-06-01")
                        .param("end", "2024-06-30"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.length()")
                .isEqualTo(0);
    }

    @Test
    @DisplayName("GET /api/v1/holidays/suggestions returns 400 when date range is invalid")
    void shouldReturn400WhenDateRangeIsInvalid() {
        given(getSuggestions.execute(any(GetHolidaySuggestionsRequest.class)))
                .willThrow(new InvalidHolidaySuggestionRangeException("Start date must be before end date"));

        assertThat(mvc.get()
                        .uri("/api/v1/holidays/suggestions")
                        .param("start", "2024-12-31")
                        .param("end", "2024-12-01"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/holidays/suggestions passes start and end dates to use case")
    void shouldPassDateParametersToUseCase() {
        given(getSuggestions.execute(any(GetHolidaySuggestionsRequest.class)))
                .willReturn(List.of());

        mvc.get()
                .uri("/api/v1/holidays/suggestions")
                .param("start", "2024-01-01")
                .param("end", "2024-01-31")
                .exchange();

        var captor = ArgumentCaptor.forClass(GetHolidaySuggestionsRequest.class);
        verify(getSuggestions).execute(captor.capture());
        assertThat(captor.getValue().start()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(captor.getValue().end()).isEqualTo(LocalDate.of(2024, 1, 31));
    }
}