package com.github.marcelorodrigo.dutytracker.gateway.controllers.oncall;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.github.marcelorodrigo.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.github.marcelorodrigo.dutytracker.infrastructure.config.AppProperties;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.GetHolidaySuggestionsUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetHolidaySuggestionsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.HolidayResponse;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(HolidayController.class)
@Import(GlobalExceptionHandler.class)
@EnableConfigurationProperties(AppProperties.class)
class HolidayControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private GetHolidaySuggestionsUseCase getSuggestions;

    @Test
    @DisplayName("should return 200 with list of holidays when suggestions exist")
    void shouldReturn200WithListOfHolidaysWhenSuggestionsExist() {
        // given
        var koningsdag = new HolidayResponse(LocalDate.of(2026, 4, 27), "Koningsdag");
        given(getSuggestions.execute(any(GetHolidaySuggestionsRequest.class))).willReturn(List.of(koningsdag));

        // when / then
        Assertions.assertThat(mvc.get()
                        .uri("/api/v1/holidays/suggestions")
                        .param("start", "2026-04-01")
                        .param("end", "2026-04-30"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("Koningsdag");
    }

    @Test
    @DisplayName("should return 200 with empty list when no holidays exist in range")
    void shouldReturn200WithEmptyListWhenNoHolidaysExistInRange() {
        // given
        given(getSuggestions.execute(any(GetHolidaySuggestionsRequest.class))).willReturn(List.of());

        // when / then
        Assertions.assertThat(mvc.get()
                        .uri("/api/v1/holidays/suggestions")
                        .param("start", "2026-03-01")
                        .param("end", "2026-03-31"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .asArray()
                .isEmpty();
    }
}
