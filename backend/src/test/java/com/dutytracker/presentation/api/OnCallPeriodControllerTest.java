package com.dutytracker.presentation.api;

import com.dutytracker.application.usecase.oncall.*;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@WebMvcTest(OnCallPeriodController.class)
@Import(GlobalExceptionHandler.class)
class OnCallPeriodControllerTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 4, 14, 14, 0);
    private static final LocalDateTime END   = LocalDateTime.of(2026, 4, 21, 14, 0);

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CreateOnCallPeriodUseCase createOnCallPeriodUseCase;

    @MockitoBean
    private GetOnCallPeriodUseCase getOnCallPeriodUseCase;

    @MockitoBean
    private ListOnCallPeriodsUseCase listOnCallPeriodsUseCase;

    @MockitoBean
    private UpdateOnCallPeriodUseCase updateOnCallPeriodUseCase;

    @MockitoBean
    private DeleteOnCallPeriodUseCase deleteOnCallPeriodUseCase;

    @MockitoBean
    private AddHolidayOverrideUseCase addHolidayOverrideUseCase;

    @MockitoBean
    private RemoveHolidayOverrideUseCase removeHolidayOverrideUseCase;

    @MockitoBean
    private CalculateOnCallDayEntriesUseCase calculateOnCallDayEntriesUseCase;

    @MockitoBean
    private OverrideOnCallDayEntryUseCase overrideOnCallDayEntryUseCase;

    // ── helpers ──────────────────────────────────────────────────────────────

    private OnCallPeriodResponse samplePeriod() {
        return new OnCallPeriodResponse(1L, START, END, List.of(), Instant.parse("2026-04-14T12:00:00Z"));
    }

    private OnCallDayEntriesResponse sampleEntries() {
        var entry = new OnCallDayEntryResponse(
                10L,
                LocalDate.of(2026, 4, 14),
                new BigDecimal("8.00"),
                StandbyRateType.WEEKDAY_SATURDAY,
                false,
                false,
                false
        );
        return new OnCallDayEntriesResponse(1L, List.of(entry));
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/oncall-periods returns 201 with Location header")
    void shouldCreatePeriod() {
        given(createOnCallPeriodUseCase.execute(any(CreateOnCallPeriodRequest.class)))
                .willReturn(samplePeriod());

        var json = """
                {
                  "startDateTime": "2026-04-14T14:00:00",
                  "endDateTime":   "2026-04-21T14:00:00"
                }
                """;

        assertThat(mvc.post().uri("/api/v1/oncall-periods")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/oncall-periods/1");
    }

    @Test
    @DisplayName("GET /api/v1/oncall-periods returns 200 with period list")
    void shouldListPeriods() {
        given(listOnCallPeriodsUseCase.execute(any(ListOnCallPeriodsRequest.class)))
                .willReturn(new OnCallPeriodListResponse(List.of(samplePeriod())));

        assertThat(mvc.get().uri("/api/v1/oncall-periods"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OnCallPeriodListResponse.class)
                .satisfies(res -> {
                    assertThat(res.periods()).hasSize(1);
                    assertThat(res.periods().get(0).id()).isEqualTo(1L);
                });
    }

    @Test
    @DisplayName("GET /api/v1/oncall-periods/1 returns 200 with period")
    void shouldGetPeriod() {
        given(getOnCallPeriodUseCase.execute(any(GetOnCallPeriodRequest.class)))
                .willReturn(samplePeriod());

        assertThat(mvc.get().uri("/api/v1/oncall-periods/1"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OnCallPeriodResponse.class)
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(1L);
                    assertThat(res.startDateTime()).isEqualTo(START);
                    assertThat(res.endDateTime()).isEqualTo(END);
                });
    }

    @Test
    @DisplayName("GET /api/v1/oncall-periods/999 returns 404 when period does not exist")
    void shouldReturn400WhenPeriodNotFound() {
        given(getOnCallPeriodUseCase.execute(any(GetOnCallPeriodRequest.class)))
                .willThrow(new InvalidOnCallPeriodException("On-call period 999 not found"));

        assertThat(mvc.get().uri("/api/v1/oncall-periods/999"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PUT /api/v1/oncall-periods/1 returns 200 with updated period")
    void shouldUpdatePeriod() {
        given(updateOnCallPeriodUseCase.execute(any(UpdateOnCallPeriodRequest.class)))
                .willReturn(samplePeriod());

        var json = """
                {
                  "startDateTime": "2026-04-14T14:00:00",
                  "endDateTime":   "2026-04-21T14:00:00"
                }
                """;

        assertThat(mvc.put().uri("/api/v1/oncall-periods/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OnCallPeriodResponse.class)
                .satisfies(res -> assertThat(res.id()).isEqualTo(1L));
    }

    @Test
    @DisplayName("DELETE /api/v1/oncall-periods/1 returns 204 No Content")
    void shouldDeletePeriod() {
        doNothing().when(deleteOnCallPeriodUseCase).execute(any(DeleteOnCallPeriodRequest.class));

        assertThat(mvc.delete().uri("/api/v1/oncall-periods/1"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("POST /api/v1/oncall-periods/1/holidays returns 200 with updated period")
    void shouldAddHoliday() {
        var periodWithHoliday = new OnCallPeriodResponse(
                1L, START, END, List.of(LocalDate.of(2026, 4, 17)), Instant.parse("2026-04-14T12:00:00Z"));
        given(addHolidayOverrideUseCase.execute(any(AddHolidayOverrideRequest.class)))
                .willReturn(periodWithHoliday);

        var json = """
                { "date": "2026-04-17" }
                """;

        assertThat(mvc.post().uri("/api/v1/oncall-periods/1/holidays")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OnCallPeriodResponse.class)
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(1L);
                    assertThat(res.holidayOverrides()).containsExactly(LocalDate.of(2026, 4, 17));
                });
    }

    @Test
    @DisplayName("DELETE /api/v1/oncall-periods/1/holidays/2026-04-17 returns 204 No Content")
    void shouldRemoveHoliday() {
        given(removeHolidayOverrideUseCase.execute(any(RemoveHolidayOverrideRequest.class)))
                .willReturn(samplePeriod());

        assertThat(mvc.delete().uri("/api/v1/oncall-periods/1/holidays/2026-04-17"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("POST /api/v1/oncall-periods/1/calculate returns 200 with day entries")
    void shouldCalculateEntries() {
        given(calculateOnCallDayEntriesUseCase.execute(any(CalculateOnCallDayEntriesRequest.class)))
                .willReturn(sampleEntries());

        assertThat(mvc.post().uri("/api/v1/oncall-periods/1/calculate"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OnCallDayEntriesResponse.class)
                .satisfies(res -> {
                    assertThat(res.periodId()).isEqualTo(1L);
                    assertThat(res.entries()).hasSize(1);
                    assertThat(res.entries().get(0).id()).isEqualTo(10L);
                    assertThat(res.entries().get(0).hours()).isEqualByComparingTo("8.00");
                });
    }

    @Test
    @DisplayName("PUT /api/v1/oncall-periods/1/day-entries/10 returns 200 with updated entry")
    void shouldOverrideDayEntry() {
        var updatedEntry = new OnCallDayEntryResponse(
                10L,
                LocalDate.of(2026, 4, 14),
                new BigDecimal("8.00"),
                StandbyRateType.WEEKDAY_SATURDAY,
                false,
                true,
                true
        );
        given(overrideOnCallDayEntryUseCase.execute(any(OverrideOnCallDayEntryRequest.class)))
                .willReturn(updatedEntry);

        var json = """
                { "timeForTimeFlag": true }
                """;

        assertThat(mvc.put().uri("/api/v1/oncall-periods/1/day-entries/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .hasStatusOk()
                .bodyJson()
                .convertTo(OnCallDayEntryResponse.class)
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(10L);
                    assertThat(res.timeForTimeFlag()).isTrue();
                    assertThat(res.manualOverride()).isTrue();
                });
    }
}
