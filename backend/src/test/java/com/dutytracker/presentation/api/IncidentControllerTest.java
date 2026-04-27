package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.incident.*;
import com.dutytracker.domain.exception.IncidentDuringWorkingHoursException;
import com.dutytracker.domain.exception.InvalidIncidentException;
import com.dutytracker.domain.exception.OvertimeDayOffException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

@WebMvcTest(IncidentController.class)
@Import(GlobalExceptionHandler.class)
class IncidentControllerTest {

    private static final LocalDate DATE       = LocalDate.of(2026, 4, 15);
    private static final LocalTime START_TIME = LocalTime.of(2, 0);
    private static final LocalTime END_TIME   = LocalTime.of(3, 45);

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private LogIncidentUseCase logIncidentUseCase;

    @MockitoBean
    private UpdateIncidentUseCase updateIncidentUseCase;

    @MockitoBean
    private DeleteIncidentUseCase deleteIncidentUseCase;

    @MockitoBean
    private ListIncidentsUseCase listIncidentsUseCase;

    @MockitoBean
    private CalculateOvertimeEntriesUseCase calculateOvertimeEntriesUseCase;

    // ── helpers ──────────────────────────────────────────────────────────────

    private IncidentResponse sampleIncident() {
        return new IncidentResponse(1L, 1L, DATE, START_TIME, END_TIME, Instant.parse("2026-04-15T02:00:00Z"));
    }

    private OvertimeEntriesResponse sampleOvertimeEntries() {
        var entry = new OvertimeEntryResponse(
                10L, 1L,
                new BigDecimal("2.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                START_TIME, END_TIME,
                false, false
        );
        return new OvertimeEntriesResponse(1L, List.of(entry));
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/incidents returns 201 with Location header")
    void shouldLogIncident() {
        given(logIncidentUseCase.execute(any(LogIncidentRequest.class)))
                .willReturn(sampleIncident());

        var json = """
                {
                  "onCallPeriodId": 1,
                  "date": "2026-04-15",
                  "startTime": "02:00:00",
                  "endTime": "03:45:00"
                }
                """;

        assertThat(mvc.post().uri("/api/v1/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/incidents/1");
    }

    @Test
    @DisplayName("GET /api/v1/incidents returns 200 with incident list")
    void shouldListAllIncidents() {
        given(listIncidentsUseCase.execute(any(ListIncidentsRequest.class)))
                .willReturn(new IncidentListResponse(List.of(sampleIncident())));

        assertThat(mvc.get().uri("/api/v1/incidents"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(IncidentListResponse.class)
                .satisfies(res -> {
                    assertThat(res.incidents()).hasSize(1);
                    assertThat(res.incidents().get(0).id()).isEqualTo(1L);
                });
    }

    @Test
    @DisplayName("GET /api/v1/incidents?onCallPeriodId=1 returns 200 with filtered list")
    void shouldListIncidentsFilteredByOnCallPeriod() {
        given(listIncidentsUseCase.execute(any(ListIncidentsRequest.class)))
                .willReturn(new IncidentListResponse(List.of(sampleIncident())));

        assertThat(mvc.get().uri("/api/v1/incidents?onCallPeriodId=1"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(IncidentListResponse.class)
                .satisfies(res -> {
                    assertThat(res.incidents()).hasSize(1);
                    assertThat(res.incidents().get(0).onCallPeriodId()).isEqualTo(1L);
                });
    }

    @Test
    @DisplayName("GET /api/v1/incidents/1 returns 200 with single incident")
    void shouldGetIncidentById() {
        given(listIncidentsUseCase.execute(any(ListIncidentsRequest.class)))
                .willReturn(new IncidentListResponse(List.of(sampleIncident())));

        assertThat(mvc.get().uri("/api/v1/incidents/1"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(IncidentResponse.class)
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(1L);
                    assertThat(res.date()).isEqualTo(DATE);
                    assertThat(res.startTime()).isEqualTo(START_TIME);
                    assertThat(res.endTime()).isEqualTo(END_TIME);
                });
    }

    @Test
    @DisplayName("GET /api/v1/incidents/999 returns 404 when incident not found")
    void shouldReturn404WhenIncidentNotFound() {
        given(listIncidentsUseCase.execute(any(ListIncidentsRequest.class)))
                .willReturn(new IncidentListResponse(List.of()));

        assertThat(mvc.get().uri("/api/v1/incidents/999"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PUT /api/v1/incidents/1 returns 200 with updated incident")
    void shouldUpdateIncident() {
        given(updateIncidentUseCase.execute(any(UpdateIncidentRequest.class)))
                .willReturn(sampleIncident());

        var json = """
                {
                  "date": "2026-04-15",
                  "startTime": "02:00:00",
                  "endTime": "03:45:00"
                }
                """;

        assertThat(mvc.put().uri("/api/v1/incidents/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(IncidentResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("DELETE /api/v1/incidents/1 returns 204 No Content")
    void shouldDeleteIncident() {
        willDoNothing().given(deleteIncidentUseCase).execute(any(DeleteIncidentRequest.class));

        assertThat(mvc.delete().uri("/api/v1/incidents/1"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("POST /api/v1/incidents/1/calculate returns 200 with overtime entries")
    void shouldCalculateOvertimeEntries() {
        given(calculateOvertimeEntriesUseCase.execute(any(CalculateOvertimeEntriesRequest.class)))
                .willReturn(sampleOvertimeEntries());

        assertThat(mvc.post().uri("/api/v1/incidents/1/calculate"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OvertimeEntriesResponse.class)
                .satisfies(res -> {
                    assertThat(res.incidentId()).isEqualTo(1L);
                    assertThat(res.entries()).hasSize(1);
                    assertThat(res.entries().get(0).id()).isEqualTo(10L);
                    assertThat(res.entries().get(0).overtimeHours()).isEqualByComparingTo("2.00");
                });
    }

    @Test
    @DisplayName("POST /api/v1/incidents/1/calculate returns 409 when incident is during working hours")
    void shouldReturn409WhenIncidentDuringWorkingHours() {
        given(calculateOvertimeEntriesUseCase.execute(any(CalculateOvertimeEntriesRequest.class)))
                .willThrow(new IncidentDuringWorkingHoursException("Incident overlaps working hours"));

        assertThat(mvc.post().uri("/api/v1/incidents/1/calculate"))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("POST /api/v1/incidents/1/calculate returns 409 when overtime falls on a day off")
    void shouldReturn409WhenOvertimeDayOff() {
        given(calculateOvertimeEntriesUseCase.execute(any(CalculateOvertimeEntriesRequest.class)))
                .willThrow(new OvertimeDayOffException("Overtime on day off is not allowed"));

        assertThat(mvc.post().uri("/api/v1/incidents/1/calculate"))
                .hasStatus(HttpStatus.CONFLICT);
    }
}
