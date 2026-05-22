package com.github.marcelorodrigo.dutytracker.gateway.controllers.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.github.marcelorodrigo.dutytracker.infrastructure.config.AppProperties;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.*;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.CreateCompensationRateUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.DeleteCompensationRateUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.GetCompensationRateTableUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.UpdateCompensationRateUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.GetCompensationRateTableRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.UpdateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateTableResponse;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(CompensationRateController.class)
@Import(GlobalExceptionHandler.class)
@EnableConfigurationProperties(AppProperties.class)
class CompensationRateControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private GetCompensationRateTableUseCase getRates;

    @MockitoBean
    private CreateCompensationRateUseCase createRate;

    @MockitoBean
    private UpdateCompensationRateUseCase updateRate;

    @MockitoBean
    private DeleteCompensationRateUseCase deleteRate;

    private CompensationRateResponse sampleRate() {
        return new CompensationRateResponse(
                1L,
                RateCategory.OVERTIME_BASE,
                OvertimeDayType.WEEKDAY,
                "Weekday Base",
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("GET /api/v1/compensation-rates returns 200 with rate table")
    void shouldGetAllRates() {
        given(getRates.execute(any(GetCompensationRateTableRequest.class)))
                .willReturn(new CompensationRateTableResponse(List.of(sampleRate())));

        assertThat(mvc.get().uri("/api/v1/compensation-rates"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CompensationRateTableResponse.class)
                .satisfies(res -> assertThat(res.rates()).hasSize(1));
    }

    @Test
    @DisplayName("POST /api/v1/compensation-rates returns 201 with created rate")
    void shouldCreateRate() {
        given(createRate.execute(any(CreateCompensationRateRequest.class))).willReturn(sampleRate());

        var json = """
                {
                  "rateCategory": "OVERTIME_BASE",
                  "overtimeDayType": "WEEKDAY",
                  "label": "Weekday Base",
                  "timeFrom": "00:00:00",
                  "timeTo": "23:59:00",
                  "percentage": 150.00
                }
                """;

        assertThat(mvc.post()
                        .uri("/api/v1/compensation-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CompensationRateResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("PUT /api/v1/compensation-rates/1 returns 200 with updated rate")
    void shouldUpdateRate() {
        var updated = new CompensationRateResponse(
                1L,
                RateCategory.OVERTIME_BASE,
                OvertimeDayType.WEEKDAY,
                "Updated Label",
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                new BigDecimal("175.00"));

        given(updateRate.execute(any(UpdateCompensationRateRequest.class))).willReturn(updated);

        var json = """
                {
                  "percentage": 175.00,
                  "label": "Updated Label"
                }
                """;

        assertThat(mvc.put()
                        .uri("/api/v1/compensation-rates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(CompensationRateResponse.class)
                .satisfies(res -> assertThat(res.label()).isEqualTo("Updated Label"));
    }

    @Test
    @DisplayName("DELETE /api/v1/compensation-rates/1 returns 204 No Content")
    void shouldDeleteRate() {
        assertThat(mvc.delete().uri("/api/v1/compensation-rates/1")).hasStatus(HttpStatus.NO_CONTENT);

        verify(deleteRate).execute(any(DeleteCompensationRateRequest.class));
    }
}
