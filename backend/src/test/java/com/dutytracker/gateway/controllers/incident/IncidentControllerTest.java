package com.dutytracker.gateway.controllers.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.dutytracker.usecase.incident.*;
import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.response.incident.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(IncidentController.class)
@Import(GlobalExceptionHandler.class)
class IncidentControllerTest {

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
    private CalculateOvertimeEntriesUseCase calculateOvertime;

    private IncidentResponse sampleIncident() {
        return new IncidentResponse(
                1L,
                10L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Instant.parse("2024-01-15T10:00:00Z"));
    }

    @Test
    @DisplayName("POST /api/v1/incidents returns 201 with created incident")
    void shouldLogIncident() {
        given(logIncident.execute(any(LogIncidentRequest.class))).willReturn(sampleIncident());

        var json = """
                {
                  "onCallPeriodId": 10,
                  "date": "2024-01-15",
                  "startTime": "09:00:00",
                  "endTime": "17:00:00"
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
        given(listIncidents.execute(any(ListIncidentsRequest.class)))
                .willReturn(new IncidentListResponse(List.of(sampleIncident())));

        assertThat(mvc.get().uri("/api/v1/incidents/1"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(IncidentResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("GET /api/v1/incidents/99 returns 404 when incident not found")
    void shouldReturn404WhenIncidentNotFound() {
        given(listIncidents.execute(any(ListIncidentsRequest.class)))
                .willReturn(new IncidentListResponse(List.of(sampleIncident())));

        assertThat(mvc.get().uri("/api/v1/incidents/99")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PUT /api/v1/incidents/1 returns 200 with updated incident")
    void shouldUpdateIncident() {
        var updated = new IncidentResponse(
                1L,
                10L,
                LocalDate.of(2024, 1, 16),
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                Instant.parse("2024-01-15T10:00:00Z"));

        given(updateIncident.execute(any(UpdateIncidentRequest.class))).willReturn(updated);

        var json = """
                {
                  "date": "2024-01-16",
                  "startTime": "10:00:00",
                  "endTime": "18:00:00"
                }
                """;

        assertThat(mvc.put()
                        .uri("/api/v1/incidents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(IncidentResponse.class)
                .satisfies(res -> assertThat(res.date()).isEqualTo(LocalDate.of(2024, 1, 16)));
    }

    @Test
    @DisplayName("DELETE /api/v1/incidents/1 returns 204 No Content")
    void shouldDeleteIncident() {
        assertThat(mvc.delete().uri("/api/v1/incidents/1")).hasStatus(HttpStatus.NO_CONTENT);

        verify(deleteIncident).execute(any(DeleteIncidentRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/incidents/1/calculate returns 200 with overtime entries")
    void shouldCalculateOvertimeEntries() {
        var overtimeEntry = new OvertimeEntryResponse(
                1L,
                1L,
                new BigDecimal("2.0"),
                new BigDecimal("1.0"),
                new BigDecimal("150.0"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false,
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
}
