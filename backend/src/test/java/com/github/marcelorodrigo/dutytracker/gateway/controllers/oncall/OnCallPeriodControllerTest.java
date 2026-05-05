package com.github.marcelorodrigo.dutytracker.gateway.controllers.oncall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.domain.StandbyRateType;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.OnCallPeriodOverlapException;
import com.github.marcelorodrigo.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.github.marcelorodrigo.dutytracker.usecase.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodReportResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

@WebMvcTest(OnCallPeriodController.class)
@Import(GlobalExceptionHandler.class)
class OnCallPeriodControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CreateOnCallPeriodUseCase createPeriod;

    @MockitoBean
    private GetOnCallPeriodUseCase getPeriod;

    @MockitoBean
    private ListOnCallPeriodsUseCase listPeriods;

    @MockitoBean
    private UpdateOnCallPeriodUseCase updatePeriod;

    @MockitoBean
    private DeleteOnCallPeriodUseCase deletePeriod;

    @MockitoBean
    private GetOnCallPeriodHolidaysUseCase getHolidays;

    @MockitoBean
    private UpdateHolidaysUseCase updateHolidays;

    @MockitoBean
    private CalculateOnCallDayEntriesUseCase calculateEntries;

    @MockitoBean
    private GenerateOnCallPeriodReportUseCase generateReport;

    private OnCallPeriodResponse samplePeriod() {
        return new OnCallPeriodResponse(
                1L,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 14, 23, 59),
                List.of(),
                LocalDateTime.of(2024, 1, 1, 10, 0, 0));
    }

    private OnCallDayEntryResponse sampleDayEntry() {
        return new OnCallDayEntryResponse(
                LocalDate.of(2024, 1, 5), "Friday", new BigDecimal("8.0"), StandbyRateType.WEEKDAY_SATURDAY, false);
    }

    @Test
    @DisplayName("POST /api/v1/oncall-periods returns 201 with created period")
    void shouldCreatePeriod() {
        given(createPeriod.execute(any(CreateOnCallPeriodRequest.class))).willReturn(samplePeriod());

        var json = """
                {
                  "startDateTime": "2024-01-01T00:00:00",
                  "endDateTime": "2024-01-14T23:59:00"
                }
                """;

        assertThat(mvc.post()
                        .uri("/api/v1/oncall-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OnCallPeriodResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("GET /api/v1/oncall-periods returns 200 with period list")
    void shouldListPeriods() {
        given(listPeriods.execute(any(ListOnCallPeriodsRequest.class)))
                .willReturn(new OnCallPeriodListResponse(List.of(samplePeriod())));

        assertThat(mvc.get().uri("/api/v1/oncall-periods"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OnCallPeriodListResponse.class)
                .satisfies(res -> assertThat(res.periods()).hasSize(1));
    }

    @Test
    @DisplayName("GET /api/v1/oncall-periods/1 returns 200 with period")
    void shouldGetPeriod() {
        given(getPeriod.execute(any(GetOnCallPeriodRequest.class))).willReturn(samplePeriod());

        assertThat(mvc.get().uri("/api/v1/oncall-periods/1"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OnCallPeriodResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("PUT /api/v1/oncall-periods/1 returns 200 with updated period")
    void shouldUpdatePeriod() {
        var updated = new OnCallPeriodResponse(
                1L,
                LocalDateTime.of(2024, 1, 2, 0, 0),
                LocalDateTime.of(2024, 1, 15, 23, 59),
                List.of(),
                LocalDateTime.of(2024, 1, 1, 10, 0, 0));

        given(updatePeriod.execute(any(UpdateOnCallPeriodRequest.class))).willReturn(updated);

        var json = """
                {
                  "startDateTime": "2024-01-02T00:00:00",
                  "endDateTime": "2024-01-15T23:59:00"
                }
                """;

        assertThat(mvc.put()
                        .uri("/api/v1/oncall-periods/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OnCallPeriodResponse.class)
                .satisfies(res -> assertThat(res.startDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0)));
    }

    @Test
    @DisplayName("DELETE /api/v1/oncall-periods/1 returns 204 No Content")
    void shouldDeletePeriod() {
        assertThat(mvc.delete().uri("/api/v1/oncall-periods/1")).hasStatus(HttpStatus.NO_CONTENT);

        verify(deletePeriod).execute(any(DeleteOnCallPeriodRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/oncall-periods/1/holidays returns 200 with holidays list")
    void shouldGetHolidays() {
        var holiday = new HolidayResponse(LocalDate.of(2024, 1, 6), "Epiphany");
        given(getHolidays.execute(any(GetOnCallPeriodHolidaysRequest.class))).willReturn(List.of(holiday));

        assertThat(mvc.get().uri("/api/v1/oncall-periods/1/holidays"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("Epiphany");
    }

    @Test
    @DisplayName("PUT /api/v1/oncall-periods/1/holidays returns 200 with saved holidays")
    void shouldUpdateHolidays() {
        var holiday = new HolidayResponse(LocalDate.of(2024, 1, 6), "Epiphany");
        given(updateHolidays.execute(any(UpdateHolidaysRequest.class))).willReturn(List.of(holiday));

        var json = """
                [
                  { "date": "2024-01-06", "name": "Epiphany" }
                ]
                """;

        assertThat(mvc.put()
                        .uri("/api/v1/oncall-periods/1/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("Epiphany");
    }

    @Test
    @DisplayName("POST /api/v1/oncall-periods/1/calculate returns 200 with day entries")
    void shouldCalculateDayEntries() {
        given(calculateEntries.execute(any(CalculateOnCallDayEntriesRequest.class)))
                .willReturn(new OnCallDayEntriesResponse(1L, List.of(sampleDayEntry())));

        assertThat(mvc.post().uri("/api/v1/oncall-periods/1/calculate"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OnCallDayEntriesResponse.class)
                .satisfies(res -> {
                    assertThat(res.periodId()).isEqualTo(1L);
                    assertThat(res.entries()).hasSize(1);
                });
    }

    @Test
    @DisplayName("GET /api/v1/oncall-periods/1/report returns 200 with full report")
    void shouldGenerateReport() {
        var report = new OnCallPeriodReportResponse(
                1L,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 14, 23, 59),
                0,
                List.of(),
                List.of(),
                List.of(sampleDayEntry()),
                List.of());

        given(generateReport.execute(any(GenerateOnCallPeriodReportRequest.class)))
                .willReturn(report);

        assertThat(mvc.get().uri("/api/v1/oncall-periods/1/report"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OnCallPeriodReportResponse.class)
                .satisfies(res -> {
                    assertThat(res.periodId()).isEqualTo(1L);
                    assertThat(res.incidentCount()).isZero();
                    assertThat(res.standbyLines()).hasSize(1);
                });
    }

    @Test
    @DisplayName("GET /api/v1/oncall-periods/99/report returns 404 when period not found")
    void shouldReturnNotFoundWhenPeriodMissingForReport() {
        given(generateReport.execute(any(GenerateOnCallPeriodReportRequest.class)))
                .willThrow(new InvalidOnCallPeriodException("OnCallPeriod not found: 99"));

        assertThat(mvc.get().uri("/api/v1/oncall-periods/99/report")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/v1/oncall-periods/1/report returns 409 when incident is during working hours")
    void shouldReturn409WhenIncidentDuringWorkingHours() {
        given(generateReport.execute(any(GenerateOnCallPeriodReportRequest.class)))
                .willThrow(new IncidentDuringWorkingHoursException());

        assertThat(mvc.get().uri("/api/v1/oncall-periods/1/report")).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("POST /api/v1/oncall-periods returns 400 when period overlaps an existing one")
    void shouldReturn400WhenPeriodOverlapsExistingOne() {
        given(createPeriod.execute(any(CreateOnCallPeriodRequest.class))).willThrow(new OnCallPeriodOverlapException());

        var json = """
                {
                  "startDateTime": "2026-05-11T14:00:00",
                  "endDateTime": "2026-05-18T14:00:00"
                }
                """;

        assertThat(mvc.post()
                        .uri("/api/v1/oncall-periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.detail")
                .isEqualTo("The requested period overlaps with an existing on-call period.");
    }

    @Test
    @DisplayName("PUT /api/v1/oncall-periods/1 returns 400 when updated period overlaps an existing one")
    void shouldReturn400WhenUpdatedPeriodOverlapsExistingOne() {
        given(updatePeriod.execute(any(UpdateOnCallPeriodRequest.class))).willThrow(new OnCallPeriodOverlapException());

        var json = """
                {
                  "startDateTime": "2026-05-11T14:00:00",
                  "endDateTime": "2026-05-18T14:00:00"
                }
                """;

        assertThat(mvc.put()
                        .uri("/api/v1/oncall-periods/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.detail")
                .isEqualTo("The requested period overlaps with an existing on-call period.");
    }
}
