package com.github.marcelorodrigo.dutytracker.gateway.controllers.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CompensationRateNotFoundException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProtectedCompensationRateException;
import com.github.marcelorodrigo.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.github.marcelorodrigo.dutytracker.gateway.controllers.TestLogCapture;
import com.github.marcelorodrigo.dutytracker.infrastructure.config.AppProperties;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.CreateCompensationRateUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.DeleteCompensationRateUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.GetCompensationRateTableUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.compensation.UpdateCompensationRateUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.CreateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.DeleteCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.GetCompensationRateTableRequest;
import com.github.marcelorodrigo.dutytracker.usecase.request.compensation.UpdateCompensationRateRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.compensation.CompensationRateTableResponse;
import java.math.BigDecimal;
import java.net.URI;
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

    private record ProblemDetailResponse(URI type, String title, int status, String detail) {}

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
    @DisplayName("should update compensation rate and log its identifier")
    void shouldUpdateRate() {
        // given
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

        // when / then
        try (var logs = TestLogCapture.forClass(CompensationRateController.class)) {
            assertThat(mvc.put()
                            .uri("/api/v1/compensation-rates/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .hasStatusOk()
                    .bodyJson()
                    .convertTo(CompensationRateResponse.class)
                    .satisfies(res -> assertThat(res.label()).isEqualTo("Updated Label"));
            assertThat(logs.keyValuePairsForMessage("Compensation rate updated"))
                    .extracting(keyValue -> keyValue.key, keyValue -> keyValue.value)
                    .containsExactly(tuple("compensationRateId", 1L));
        }
    }

    @Test
    @DisplayName("should delete compensation rate and log its identifier")
    void shouldDeleteRate() {
        // given - the delete use case completes successfully

        // when / then
        try (var logs = TestLogCapture.forClass(CompensationRateController.class)) {
            assertThat(mvc.delete().uri("/api/v1/compensation-rates/1")).hasStatus(HttpStatus.NO_CONTENT);
            verify(deleteRate).execute(any(DeleteCompensationRateRequest.class));
            assertThat(logs.keyValuePairsForMessage("Compensation rate deleted"))
                    .extracting(keyValue -> keyValue.key, keyValue -> keyValue.value)
                    .containsExactly(tuple("compensationRateId", 1L));
        }
    }

    @Test
    @DisplayName("should not log rate deletion when compensation rate is protected")
    void shouldNotLogRateDeletionWhenCompensationRateIsProtected() {
        // given
        given(deleteRate.execute(any(DeleteCompensationRateRequest.class)))
                .willThrow(new ProtectedCompensationRateException(1L));

        // when / then
        try (var logs = TestLogCapture.forClass(CompensationRateController.class)) {
            assertThat(mvc.delete().uri("/api/v1/compensation-rates/1"))
                    .hasStatus(HttpStatus.CONFLICT)
                    .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyJson()
                    .convertTo(ProblemDetailResponse.class)
                    .satisfies(problem -> {
                        assertThat(problem.type())
                                .isEqualTo(URI.create("http://localhost:8080/errors/protected-compensation-rate"));
                        assertThat(problem.title()).isEqualTo("Protected compensation rate");
                        assertThat(problem.status()).isEqualTo(409);
                        assertThat(problem.detail())
                                .isEqualTo(
                                        "Compensation rate 1 is protected and cannot be deleted; only OVERTIME_ALLOWANCE rates may be deleted");
                    });
            assertThat(logs.eventsWithMessage("Compensation rate deleted")).isEmpty();
        }
    }

    @Test
    @DisplayName("should not log rate update when compensation rate is missing")
    void shouldNotLogRateUpdateWhenCompensationRateIsMissing() {
        // given
        given(updateRate.execute(any(UpdateCompensationRateRequest.class)))
                .willThrow(new CompensationRateNotFoundException("Rate not found: 1"));
        var json = """
                {
                  "percentage": 175.00,
                  "label": "Updated Label"
                }
                """;

        // when / then
        try (var logs = TestLogCapture.forClass(CompensationRateController.class)) {
            assertThat(mvc.put()
                            .uri("/api/v1/compensation-rates/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .hasStatus(HttpStatus.NOT_FOUND);
            assertThat(logs.eventsWithMessage("Compensation rate updated")).isEmpty();
        }
    }
}
