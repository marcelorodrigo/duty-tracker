package com.github.marcelorodrigo.dutytracker.gateway.controllers.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.controllers.CorrelationIdFilter;
import com.github.marcelorodrigo.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.github.marcelorodrigo.dutytracker.gateway.controllers.TestLogCapture;
import com.github.marcelorodrigo.dutytracker.infrastructure.config.AppProperties;
import com.github.marcelorodrigo.dutytracker.usecase.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.IncidentResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(IncidentController.class)
@Import(GlobalExceptionHandler.class)
@EnableConfigurationProperties(AppProperties.class)
class IncidentControllerTest {

    private static final String PROBLEM_BASE_URL = "http://localhost:8080/errors/";

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private LogIncidentUseCase logIncident;

    @MockitoBean
    private UpdateIncidentUseCase updateIncident;

    @MockitoBean
    private DeleteIncidentUseCase deleteIncident;

    @MockitoBean
    private ListIncidentsUseCase listIncidents;

    @MockitoBean
    private GetIncidentUseCase getIncident;

    @MockitoBean
    private CalculateOvertimeEntriesUseCase calculateOvertime;

    private record ProblemDetailResponse(URI type, String title, int status, String detail, URI instance) {}

    private static void assertProblemDetail(
            ProblemDetailResponse problem, String type, String title, int status, String detail) {
        assertThat(problem.type()).isEqualTo(URI.create(PROBLEM_BASE_URL + type));
        assertThat(problem.title()).isEqualTo(title);
        assertThat(problem.status()).isEqualTo(status);
        assertThat(problem.detail()).isEqualTo(detail);
        assertThat(problem.instance()).isEqualTo(URI.create("/api/v1/incidents"));
    }

    private static void assertIncidentNotFoundProblem(ProblemDetailResponse problem, String instance) {
        assertThat(problem.type()).isEqualTo(URI.create(PROBLEM_BASE_URL + "incident-not-found"));
        assertThat(problem.title()).isEqualTo("Incident not found");
        assertThat(problem.status()).isEqualTo(404);
        assertThat(problem.detail()).isEqualTo("Incident not found: 99");
        assertThat(problem.instance()).isEqualTo(URI.create(instance));
    }

    private IncidentResponse sampleIncident() {
        return new IncidentResponse(
                1L,
                10L,
                "Network outage",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 17, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0, 0));
    }

    @Test
    @DisplayName("POST /api/v1/incidents returns 201 with created incident")
    void shouldLogIncident() {
        given(logIncident.execute(any(LogIncidentRequest.class))).willReturn(sampleIncident());

        var json = """
                {
                  "onCallPeriodId": 10,
                  "name": "Network outage",
                  "startDateTime": "2024-01-15T09:00:00",
                  "endDateTime": "2024-01-15T17:00:00"
                }
                """;

        assertThat(mvc.post()
                        .uri("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(IncidentResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("GET /api/v1/incidents returns 200 with incident list")
    void shouldListIncidents() {
        given(listIncidents.execute(any(ListIncidentsRequest.class)))
                .willReturn(new IncidentListResponse(List.of(sampleIncident())));

        assertThat(mvc.get().uri("/api/v1/incidents"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(IncidentListResponse.class)
                .satisfies(res -> assertThat(res.incidents()).hasSize(1));
    }

    @Test
    @DisplayName("GET /api/v1/incidents/1 returns 200 when incident exists")
    void shouldGetIncidentById() {
        given(getIncident.execute(any(GetIncidentRequest.class))).willReturn(sampleIncident());

        assertThat(mvc.get().uri("/api/v1/incidents/1"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(IncidentResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("should update incident and log its identifier")
    void shouldUpdateIncident() {
        // given
        var updated = new IncidentResponse(
                1L,
                10L,
                "Network outage",
                LocalDateTime.of(2024, 1, 16, 10, 0),
                LocalDateTime.of(2024, 1, 16, 18, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0, 0));

        given(updateIncident.execute(any(UpdateIncidentRequest.class))).willReturn(updated);

        var json = """
                {
                  "name": "Network outage",
                  "startDateTime": "2024-01-16T10:00:00",
                  "endDateTime": "2024-01-16T18:00:00"
                }
                """;

        // when / then
        try (var logs = TestLogCapture.forClass(IncidentController.class)) {
            assertThat(mvc.put()
                            .uri("/api/v1/incidents/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .hasStatusOk()
                    .bodyJson()
                    .convertTo(IncidentResponse.class)
                    .satisfies(res -> assertThat(res.startDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 16, 10, 0)));
            assertThat(logs.keyValuePairsForMessage("Incident updated"))
                    .extracting(keyValue -> keyValue.key, keyValue -> keyValue.value)
                    .containsExactly(tuple("incidentId", 1L));
        }
    }

    @Test
    @DisplayName("should delete incident and log its identifier")
    void shouldDeleteIncident() {
        // given - the delete use case completes successfully

        // when / then
        try (var logs = TestLogCapture.forClass(IncidentController.class)) {
            assertThat(mvc.delete().uri("/api/v1/incidents/1")).hasStatus(HttpStatus.NO_CONTENT);
            verify(deleteIncident).execute(any(DeleteIncidentRequest.class));
            assertThat(logs.keyValuePairsForMessage("Incident deleted"))
                    .extracting(keyValue -> keyValue.key, keyValue -> keyValue.value)
                    .containsExactly(tuple("incidentId", 1L));
        }
    }

    @Test
    @DisplayName("POST /api/v1/incidents/1/calculate returns 200 with overtime entries")
    void shouldCalculateOvertimeEntries() {
        var overtimeEntry = new OvertimeEntryResponse(
                1L,
                new BigDecimal("2.0"),
                new BigDecimal("1.0"),
                new BigDecimal("150.0"),
                LocalDate.of(2026, 4, 14),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false);
        given(calculateOvertime.execute(any(CalculateOvertimeEntriesRequest.class)))
                .willReturn(new OvertimeEntriesResponse(1L, List.of(overtimeEntry)));

        assertThat(mvc.post().uri("/api/v1/incidents/1/calculate"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OvertimeEntriesResponse.class)
                .satisfies(res -> {
                    assertThat(res.incidentId()).isEqualTo(1L);
                    assertThat(res.entries()).hasSize(1);
                });
    }

    @Test
    @DisplayName("should return the incident-not-found problem when getting an unknown incident")
    void shouldReturnIncidentNotFoundProblemWhenGettingUnknownIncident() {
        // given
        given(getIncident.execute(any(GetIncidentRequest.class))).willThrow(new IncidentNotFoundException(99L));

        // when / then
        assertThat(mvc.get().uri("/api/v1/incidents/99"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetailResponse.class)
                .satisfies(problem -> assertIncidentNotFoundProblem(problem, "/api/v1/incidents/99"));
    }

    @Test
    @DisplayName("should not log an incident update when updating an unknown incident")
    void shouldNotLogIncidentUpdateWhenUpdatingUnknownIncident() {
        // given
        given(updateIncident.execute(any(UpdateIncidentRequest.class))).willThrow(new IncidentNotFoundException(99L));
        var json = """
                {
                  "name": "Network outage",
                  "startDateTime": "2024-01-16T10:00:00",
                  "endDateTime": "2024-01-16T18:00:00"
                }
                """;

        // when / then
        try (var logs = TestLogCapture.forClass(IncidentController.class)) {
            assertThat(mvc.put()
                            .uri("/api/v1/incidents/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .hasStatus(HttpStatus.NOT_FOUND)
                    .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyJson()
                    .convertTo(ProblemDetailResponse.class)
                    .satisfies(problem -> assertIncidentNotFoundProblem(problem, "/api/v1/incidents/99"));
            assertThat(logs.eventsWithMessage("Incident updated")).isEmpty();
        }
    }

    @Test
    @DisplayName("should not log incident deletion when deleting an unknown incident")
    void shouldNotLogIncidentDeletionWhenDeletingUnknownIncident() {
        // given
        given(deleteIncident.execute(any(DeleteIncidentRequest.class))).willThrow(new IncidentNotFoundException(99L));

        // when / then
        try (var logs = TestLogCapture.forClass(IncidentController.class)) {
            assertThat(mvc.delete().uri("/api/v1/incidents/99"))
                    .hasStatus(HttpStatus.NOT_FOUND)
                    .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyJson()
                    .convertTo(ProblemDetailResponse.class)
                    .satisfies(problem -> assertIncidentNotFoundProblem(problem, "/api/v1/incidents/99"));
            assertThat(logs.eventsWithMessage("Incident deleted")).isEmpty();
        }
    }

    @Test
    @DisplayName("should return the incident-not-found problem when calculating an unknown incident")
    void shouldReturnIncidentNotFoundProblemWhenCalculatingUnknownIncident() {
        // given
        given(calculateOvertime.execute(any(CalculateOvertimeEntriesRequest.class)))
                .willThrow(new IncidentNotFoundException(99L));

        // when / then
        assertThat(mvc.post().uri("/api/v1/incidents/99/calculate"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetailResponse.class)
                .satisfies(problem -> assertIncidentNotFoundProblem(problem, "/api/v1/incidents/99/calculate"));
    }

    @Test
    @DisplayName("should return a problem detail when request body validation fails")
    void shouldReturnProblemDetailWhenRequestBodyValidationFails() {
        // given
        var json = """
                {
                  "name": "Network outage",
                  "startDateTime": "2024-01-15T09:00:00",
                  "endDateTime": "2024-01-15T17:00:00"
                }
                """;

        // when / then
        assertThat(mvc.post()
                        .uri("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetailResponse.class)
                .satisfies(problem -> assertProblemDetail(
                        problem,
                        "request-validation-failed",
                        "Request validation failed",
                        400,
                        "One or more request values are invalid."));
    }

    @ParameterizedTest
    @MethodSource("requestsWithInvalidIncidentDates")
    @DisplayName("should return a problem detail when an incident date is missing or null")
    void shouldReturnProblemDetailWhenIncidentDateIsMissingOrNull(String json) {
        // given - invalid request JSON supplied by the method source

        // when / then
        assertThat(mvc.post()
                        .uri("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetailResponse.class)
                .satisfies(problem -> assertProblemDetail(
                        problem,
                        "request-validation-failed",
                        "Request validation failed",
                        400,
                        "One or more request values are invalid."));
    }

    private static Stream<String> requestsWithInvalidIncidentDates() {
        return Stream.of("""
                {
                  "onCallPeriodId": 10,
                  "name": "Network outage",
                  "endDateTime": "2024-01-15T17:00:00"
                }
                """, """
                {
                  "onCallPeriodId": 10,
                  "name": "Network outage",
                  "startDateTime": null,
                  "endDateTime": "2024-01-15T17:00:00"
                }
                """, """
                {
                  "onCallPeriodId": 10,
                  "name": "Network outage",
                  "startDateTime": "2024-01-15T09:00:00"
                }
                """, """
                {
                  "onCallPeriodId": 10,
                  "name": "Network outage",
                  "startDateTime": "2024-01-15T09:00:00",
                  "endDateTime": null
                }
                """);
    }

    @Test
    @DisplayName("should return a problem detail when a request constraint is violated")
    void shouldReturnProblemDetailWhenRequestConstraintIsViolated() {
        // given
        given(listIncidents.execute(any(ListIncidentsRequest.class)))
                .willThrow(new ConstraintViolationException("A request constraint failed", Set.of()));

        // when / then
        assertThat(mvc.get().uri("/api/v1/incidents"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetailResponse.class)
                .satisfies(problem -> assertProblemDetail(
                        problem,
                        "constraint-violation",
                        "Request constraint violation",
                        400,
                        "One or more request constraints were violated."));
    }

    @Test
    @DisplayName("should return a problem detail when request JSON is malformed")
    void shouldReturnProblemDetailWhenRequestJsonIsMalformed() {
        // given
        var malformedJson = "{\"onCallPeriodId\":";

        // when / then
        assertThat(mvc.post()
                        .uri("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetailResponse.class)
                .satisfies(problem -> assertProblemDetail(
                        problem,
                        "malformed-request",
                        "Malformed request body",
                        400,
                        "The request body is malformed or unreadable."));
    }

    @Test
    @DisplayName("should return a sanitized problem detail when an unexpected error occurs")
    void shouldReturnSanitizedProblemDetailWhenUnexpectedErrorOccurs() {
        // given
        given(listIncidents.execute(any(ListIncidentsRequest.class)))
                .willThrow(new IllegalStateException("database password=super-secret"));

        // when / then
        assertThat(mvc.get().uri("/api/v1/incidents").header("X-Correlation-ID", "incident-list-123"))
                .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .hasHeader(CorrelationIdFilter.HEADER_NAME, "incident-list-123")
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetailResponse.class)
                .satisfies(problem -> {
                    assertProblemDetail(
                            problem,
                            "internal-server-error",
                            "Internal server error",
                            500,
                            "An unexpected error occurred.");
                    assertThat(problem.detail()).doesNotContain("super-secret");
                });
    }
}
