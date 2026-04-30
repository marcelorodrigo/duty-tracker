package com.dutytracker.gateway.controllers.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.dutytracker.domain.StandbyRateType;
import com.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.dutytracker.usecase.oncall.OverrideOnCallDayEntryUseCase;
import com.dutytracker.usecase.request.oncall.OverrideOnCallDayEntryRequest;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.dutytracker.usecase.response.oncall.OnCallDayEntryResponse;
import com.dutytracker.usecase.response.summary.*;
import com.dutytracker.usecase.summary.*;
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

@WebMvcTest(RegistrationSummaryController.class)
@Import(GlobalExceptionHandler.class)
class RegistrationSummaryControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CreateRegistrationSummaryUseCase createSummary;

    @MockitoBean
    private GetRegistrationSummaryUseCase getSummary;

    @MockitoBean
    private ListRegistrationSummariesUseCase listSummaries;

    @MockitoBean
    private DeleteRegistrationSummaryUseCase deleteSummary;

    @MockitoBean
    private OverrideOnCallDayEntryUseCase overrideOnCallEntry;

    @MockitoBean
    private DeleteOnCallDayEntryUseCase deleteOnCallEntry;

    @MockitoBean
    private OverrideOvertimeEntryUseCase overrideOvertimeEntry;

    @MockitoBean
    private DeleteOvertimeEntryUseCase deleteOvertimeEntry;

    @MockitoBean
    private AddOnCallDayEntryUseCase addOnCallEntry;

    @MockitoBean
    private AddOvertimeEntryUseCase addOvertimeEntry;

    private RegistrationSummaryResponse sampleSummary() {
        return new RegistrationSummaryResponse(
                1L,
                "January Summary",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 14),
                Instant.parse("2024-01-15T10:00:00Z"),
                Instant.parse("2024-01-15T10:00:00Z"),
                List.of(),
                List.of());
    }

    private OnCallDayEntryResponse sampleOnCallEntry() {
        return new OnCallDayEntryResponse(
                1L,
                LocalDate.of(2024, 1, 5),
                new BigDecimal("8.0"),
                StandbyRateType.WEEKDAY_SATURDAY,
                false,
                false,
                false);
    }

    private OvertimeEntryResponse sampleOvertimeEntry() {
        return new OvertimeEntryResponse(
                1L,
                1L,
                new BigDecimal("2.0"),
                new BigDecimal("1.0"),
                new BigDecimal("150.0"),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false,
                false);
    }

    @Test
    @DisplayName("POST /api/v1/summaries returns 201 with created summary")
    void shouldCreateSummary() {
        given(createSummary.execute(any(CreateRegistrationSummaryRequest.class)))
                .willReturn(sampleSummary());

        var json = """
                {
                  "periodId": 10,
                  "label": "January Summary"
                }
                """;

        assertThat(mvc.post()
                        .uri("/api/v1/summaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(RegistrationSummaryResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("GET /api/v1/summaries returns 200 with summary list")
    void shouldListSummaries() {
        given(listSummaries.execute(any(ListRegistrationSummariesRequest.class)))
                .willReturn(new RegistrationSummaryListResponse(List.of(sampleSummary())));

        assertThat(mvc.get().uri("/api/v1/summaries"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(RegistrationSummaryListResponse.class)
                .satisfies(res -> assertThat(res.summaries()).hasSize(1));
    }

    @Test
    @DisplayName("GET /api/v1/summaries/1 returns 200 with summary")
    void shouldGetSummary() {
        given(getSummary.execute(any(GetRegistrationSummaryRequest.class))).willReturn(sampleSummary());

        assertThat(mvc.get().uri("/api/v1/summaries/1"))
                .hasStatusOk()
                .bodyJson()
                .convertTo(RegistrationSummaryResponse.class)
                .satisfies(res -> assertThat(res.label()).isEqualTo("January Summary"));
    }

    @Test
    @DisplayName("DELETE /api/v1/summaries/1 returns 204 No Content")
    void shouldDeleteSummary() {
        assertThat(mvc.delete().uri("/api/v1/summaries/1")).hasStatus(HttpStatus.NO_CONTENT);

        verify(deleteSummary).execute(any(DeleteRegistrationSummaryRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/summaries/1/oncall-entries returns 201 with created entry")
    void shouldAddOnCallEntry() {
        given(addOnCallEntry.execute(any(AddOnCallDayEntryRequest.class))).willReturn(sampleOnCallEntry());

        var json = """
                {
                  "date": "2024-01-05",
                  "hours": 8.0,
                  "rateType": "WEEKDAY_SATURDAY"
                }
                """;

        assertThat(mvc.post()
                        .uri("/api/v1/summaries/1/oncall-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(OnCallDayEntryResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("PUT /api/v1/summaries/1/oncall-entries/1 returns 200 with overridden entry")
    void shouldOverrideOnCallEntry() {
        given(overrideOnCallEntry.execute(any(OverrideOnCallDayEntryRequest.class)))
                .willReturn(sampleOnCallEntry());

        var json = """
                {
                  "hours": 8.0,
                  "rateType": "WEEKDAY_SATURDAY",
                  "timeForTimeFlag": false
                }
                """;

        assertThat(mvc.put()
                        .uri("/api/v1/summaries/1/oncall-entries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OnCallDayEntryResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("DELETE /api/v1/summaries/1/oncall-entries/1 returns 204 No Content")
    void shouldDeleteOnCallEntry() {
        assertThat(mvc.delete().uri("/api/v1/summaries/1/oncall-entries/1")).hasStatus(HttpStatus.NO_CONTENT);

        verify(deleteOnCallEntry).execute(any(DeleteOnCallDayEntryRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/summaries/1/overtime-entries returns 201 with created entry")
    void shouldAddOvertimeEntry() {
        given(addOvertimeEntry.execute(any(AddOvertimeEntryRequest.class))).willReturn(sampleOvertimeEntry());

        var json = """
                {
                  "incidentId": 1,
                  "overtimeHours": 2.0,
                  "allowanceHours": 1.0,
                  "allowancePercentage": 150.0,
                  "timeFrom": "09:00:00",
                  "timeTo": "17:00:00",
                  "isAllowanceEntry": false
                }
                """;

        assertThat(mvc.post()
                        .uri("/api/v1/summaries/1/overtime-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(OvertimeEntryResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("PUT /api/v1/summaries/1/overtime-entries/1 returns 200 with overridden entry")
    void shouldOverrideOvertimeEntry() {
        given(overrideOvertimeEntry.execute(any(OverrideOvertimeEntryRequest.class)))
                .willReturn(sampleOvertimeEntry());

        var json = """
                {
                  "overtimeHours": 3.0,
                  "allowanceHours": 1.5,
                  "allowancePercentage": 150.0
                }
                """;

        assertThat(mvc.put()
                        .uri("/api/v1/summaries/1/overtime-entries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OvertimeEntryResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("DELETE /api/v1/summaries/1/overtime-entries/1 returns 204 No Content")
    void shouldDeleteOvertimeEntry() {
        assertThat(mvc.delete().uri("/api/v1/summaries/1/overtime-entries/1")).hasStatus(HttpStatus.NO_CONTENT);

        verify(deleteOvertimeEntry).execute(any(DeleteOvertimeEntryRequest.class));
    }
}
