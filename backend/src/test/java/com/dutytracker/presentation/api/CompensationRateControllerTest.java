package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.compensation.*;
import com.dutytracker.domain.model.EmployeeType;
import com.dutytracker.domain.model.RateCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebMvcTest(CompensationRateController.class)
class CompensationRateControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private GetCompensationRateTableUseCase getCompensationRateTableUseCase;

    @MockitoBean
    private CreateCompensationRateUseCase createCompensationRateUseCase;

    @MockitoBean
    private UpdateCompensationRateUseCase updateCompensationRateUseCase;

    @MockitoBean
    private DeleteCompensationRateUseCase deleteCompensationRateUseCase;

    private CompensationRateResponse sampleRate() {
        return new CompensationRateResponse(1L, EmployeeType.INTERNAL, RateCategory.ONCALL_WEEKDAY_SATURDAY,
                "Weekday on-call", null, null, java.math.BigDecimal.ZERO);
    }

    @Test
    @DisplayName("GET /api/v1/compensation-rates returns 200 with rate table")
    void shouldReturnCompensationRateTable() {
        var tableResponse = new CompensationRateTableResponse(List.of(sampleRate()));
        given(getCompensationRateTableUseCase.execute(any(GetCompensationRateTableRequest.class)))
                .willReturn(tableResponse);

        assertThat(mvc.get().uri("/api/v1/compensation-rates"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CompensationRateTableResponse.class)
                .satisfies(res -> assertThat(res.rates()).hasSize(1));
    }

    @Test
    @DisplayName("GET /api/v1/compensation-rates?employeeType=INTERNAL filters by employee type")
    void shouldFilterRatesByEmployeeType() {
        var tableResponse = new CompensationRateTableResponse(List.of(sampleRate()));
        given(getCompensationRateTableUseCase.execute(any(GetCompensationRateTableRequest.class)))
                .willReturn(tableResponse);

        assertThat(mvc.get().uri("/api/v1/compensation-rates?employeeType=INTERNAL"))
                .hasStatusOk();

        verify(getCompensationRateTableUseCase).execute(new GetCompensationRateTableRequest(EmployeeType.INTERNAL));
    }

    @Test
    @DisplayName("POST /api/v1/compensation-rates returns 201 Created with Location header")
    void shouldCreateCompensationRate() {
        given(createCompensationRateUseCase.execute(any(CreateCompensationRateRequest.class)))
                .willReturn(sampleRate());

        var json = """
                {
                  "employeeType": "INTERNAL",
                  "rateCategory": "OVERTIME_ALLOWANCE",
                  "label": "Weekday evening",
                  "timeFrom": "18:00",
                  "timeTo": "22:00",
                  "percentage": "0.0000"
                }
                """;

        assertThat(mvc.post().uri("/api/v1/compensation-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/compensation-rates/1");
    }

    @Test
    @DisplayName("PUT /api/v1/compensation-rates/{id} returns 200 with updated rate")
    void shouldUpdateCompensationRate() {
        var updated = new CompensationRateResponse(1L, EmployeeType.INTERNAL, RateCategory.ONCALL_WEEKDAY_SATURDAY,
                "Updated label", null, null, new java.math.BigDecimal("0.2500"));
        given(updateCompensationRateUseCase.execute(any(UpdateCompensationRateRequest.class)))
                .willReturn(updated);

        var json = """
                {
                  "percentage": "0.2500",
                  "label": "Updated label"
                }
                """;

        assertThat(mvc.put().uri("/api/v1/compensation-rates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(CompensationRateResponse.class)
                .satisfies(res -> assertThat(res.percentage()).isEqualTo("0.2500"));
    }

    @Test
    @DisplayName("DELETE /api/v1/compensation-rates/{id} returns 204 No Content")
    void shouldDeleteCompensationRate() {
        assertThat(mvc.delete().uri("/api/v1/compensation-rates/1"))
                .hasStatus(HttpStatus.NO_CONTENT);

        verify(deleteCompensationRateUseCase).execute(new DeleteCompensationRateRequest(1L));
    }
}
