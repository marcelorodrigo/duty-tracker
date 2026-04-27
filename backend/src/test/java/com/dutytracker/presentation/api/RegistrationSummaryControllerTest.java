package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.incident.OvertimeEntryResponse;
import com.dutytracker.application.usecase.oncall.OnCallDayEntryResponse;
import com.dutytracker.application.usecase.oncall.OverrideOnCallDayEntryRequest;
import com.dutytracker.application.usecase.oncall.OverrideOnCallDayEntryUseCase;
import com.dutytracker.application.usecase.summary.*;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.model.StandbyRateType;
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

@WebMvcTest(RegistrationSummaryController.class)
@Import(GlobalExceptionHandler.class)
class RegistrationSummaryControllerTest {

    private static final LocalDate PERIOD_START = LocalDate.of(2026, 4, 13);
    private static final LocalDate PERIOD_END   = LocalDate.of(2026, 4, 19);

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean private CreateRegistrationSummaryUseCase createRegistrationSummaryUseCase;
    @MockitoBean private GetRegistrationSummaryUseCase getRegistrationSummaryUseCase;
    @MockitoBean private ListRegistrationSummariesUseCase listRegistrationSummariesUseCase;
    @MockitoBean private DeleteRegistrationSummaryUseCase deleteRegistrationSummaryUseCase;
    @MockitoBean private OverrideOnCallDayEntryUseCase overrideOnCallDayEntryUseCase;
    @MockitoBean private DeleteOnCallDayEntryUseCase deleteOnCallDayEntryUseCase;
    @MockitoBean private OverrideOvertimeEntryUseCase overrideOvertimeEntryUseCase;
    @MockitoBean private DeleteOvertimeEntryUseCase deleteOvertimeEntryUseCase;
    @MockitoBean private AddOnCallDayEntryUseCase addOnCallDayEntryUseCase;
    @MockitoBean private AddOvertimeEntryUseCase addOvertimeEntryUseCase;

    // ── helpers ───────────────────────────────────────────────────────────────

    private RegistrationSummaryResponse sampleSummary() {
        return new RegistrationSummaryResponse(
                1L, "Week 16", PERIOD_START, PERIOD_END,
                Instant.parse("2026-04-13T10:00:00Z"), Instant.parse("2026-04-13T10:00:00Z"),
                List.of(), List.of());
    }

    private OnCallDayEntryResponse sampleOnCallEntry() {
        return new OnCallDayEntryResponse(
                10L, LocalDate.of(2026, 4, 14),
                new BigDecimal("8.00"), StandbyRateType.WEEKDAY_SATURDAY,
                false, false, false);
    }

    private OvertimeEntryResponse sampleOvertimeEntry() {
        return new OvertimeEntryResponse(
                20L, 5L,
                new BigDecimal("2.00"), new BigDecimal("1.00"), new BigDecimal("0.25"),
                LocalTime.of(18, 0), LocalTime.of(20, 0),
                false, false);
    }

    // ── Summary CRUD ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/summaries returns 201 with Location header")
    void shouldCreateSummary() {
        given(createRegistrationSummaryUseCase.execute(any(CreateRegistrationSummaryRequest.class)))
                .willReturn(sampleSummary());

        var json = """
                { "periodId": 1, "label": "Week 16" }
                """;

        assertThat(mvc.post().uri("/api/v1/summaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/summaries/1");
    }

    @Test
    @DisplayName("GET /api/v1/summaries returns 200 with summary list")
    void shouldListSummaries() {
        given(listRegistrationSummariesUseCase.execute(any(ListRegistrationSummariesRequest.class)))
                .willReturn(new RegistrationSummaryListResponse(List.of(sampleSummary())));

        assertThat(mvc.get().uri("/api/v1/summaries"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(RegistrationSummaryListResponse.class)
                .satisfies(res -> {
                    assertThat(res.summaries()).hasSize(1);
                    assertThat(res.summaries().get(0).id()).isEqualTo(1L);
                    assertThat(res.summaries().get(0).label()).isEqualTo("Week 16");
                });
    }

    @Test
    @DisplayName("GET /api/v1/summaries/1 returns 200 with summary")
    void shouldGetSummary() {
        given(getRegistrationSummaryUseCase.execute(any(GetRegistrationSummaryRequest.class)))
                .willReturn(sampleSummary());

        assertThat(mvc.get().uri("/api/v1/summaries/1"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(RegistrationSummaryResponse.class)
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(1L);
                    assertThat(res.label()).isEqualTo("Week 16");
                    assertThat(res.periodStart()).isEqualTo(PERIOD_START);
                    assertThat(res.periodEnd()).isEqualTo(PERIOD_END);
                });
    }

    @Test
    @DisplayName("GET /api/v1/summaries/999 returns 404 when summary does not exist")
    void shouldReturn400WhenSummaryNotFound() {
        given(getRegistrationSummaryUseCase.execute(any(GetRegistrationSummaryRequest.class)))
                .willThrow(new InvalidOnCallPeriodException("Summary not found"));

        assertThat(mvc.get().uri("/api/v1/summaries/999"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /api/v1/summaries/1 returns 204 No Content")
    void shouldDeleteSummary() {
        willDoNothing().given(deleteRegistrationSummaryUseCase).execute(any(DeleteRegistrationSummaryRequest.class));

        assertThat(mvc.delete().uri("/api/v1/summaries/1"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    // ── On-Call Day Entries ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/summaries/1/oncall-entries returns 201")
    void shouldAddOnCallEntry() {
        given(addOnCallDayEntryUseCase.execute(any(AddOnCallDayEntryRequest.class)))
                .willReturn(sampleOnCallEntry());

        var json = """
                { "date": "2026-04-14", "hours": 8.00, "rateType": "WEEKDAY_SATURDAY" }
                """;

        assertThat(mvc.post().uri("/api/v1/summaries/1/oncall-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(OnCallDayEntryResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(10L));
    }

    @Test
    @DisplayName("PUT /api/v1/summaries/1/oncall-entries/10 returns 200 with updated entry")
    void shouldOverrideOnCallEntry() {
        var updated = new OnCallDayEntryResponse(
                10L, LocalDate.of(2026, 4, 14),
                new BigDecimal("4.00"), StandbyRateType.SUNDAY_HOLIDAY,
                false, false, true);
        given(overrideOnCallDayEntryUseCase.execute(any(OverrideOnCallDayEntryRequest.class)))
                .willReturn(updated);

        var json = """
                { "hours": 4.00, "rateType": "SUNDAY_HOLIDAY" }
                """;

        assertThat(mvc.put().uri("/api/v1/summaries/1/oncall-entries/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OnCallDayEntryResponse.class)
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(10L);
                    assertThat(res.manualOverride()).isTrue();
                });
    }

    @Test
    @DisplayName("DELETE /api/v1/summaries/1/oncall-entries/10 returns 204 No Content")
    void shouldDeleteOnCallEntry() {
        willDoNothing().given(deleteOnCallDayEntryUseCase).execute(any(DeleteOnCallDayEntryRequest.class));

        assertThat(mvc.delete().uri("/api/v1/summaries/1/oncall-entries/10"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    // ── Overtime Entries ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/summaries/1/overtime-entries returns 201")
    void shouldAddOvertimeEntry() {
        given(addOvertimeEntryUseCase.execute(any(AddOvertimeEntryRequest.class)))
                .willReturn(sampleOvertimeEntry());

        var json = """
                {
                  "incidentId": 5,
                  "overtimeHours": 2.00,
                  "allowanceHours": 1.00,
                  "allowancePercentage": 0.25,
                  "timeFrom": "18:00:00",
                  "timeTo": "20:00:00",
                  "isAllowanceEntry": false
                }
                """;

        assertThat(mvc.post().uri("/api/v1/summaries/1/overtime-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(OvertimeEntryResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(20L));
    }

    @Test
    @DisplayName("PUT /api/v1/summaries/1/overtime-entries/20 returns 200 with updated entry")
    void shouldOverrideOvertimeEntry() {
        var updated = new OvertimeEntryResponse(
                20L, 5L,
                new BigDecimal("3.00"), new BigDecimal("1.50"), new BigDecimal("0.30"),
                LocalTime.of(18, 0), LocalTime.of(21, 0),
                false, true);
        given(overrideOvertimeEntryUseCase.execute(any(OverrideOvertimeEntryRequest.class)))
                .willReturn(updated);

        var json = """
                { "overtimeHours": 3.00, "allowanceHours": 1.50, "allowancePercentage": 0.30 }
                """;

        assertThat(mvc.put().uri("/api/v1/summaries/1/overtime-entries/20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OvertimeEntryResponse.class)
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(20L);
                    assertThat(res.manualOverride()).isTrue();
                });
    }

    @Test
    @DisplayName("DELETE /api/v1/summaries/1/overtime-entries/20 returns 204 No Content")
    void shouldDeleteOvertimeEntry() {
        willDoNothing().given(deleteOvertimeEntryUseCase).execute(any(DeleteOvertimeEntryRequest.class));

        assertThat(mvc.delete().uri("/api/v1/summaries/1/overtime-entries/20"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }
}
