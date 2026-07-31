package com.github.marcelorodrigo.dutytracker.gateway.controllers.calendarfeed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.github.marcelorodrigo.dutytracker.gateway.controllers.GlobalExceptionHandler;
import com.github.marcelorodrigo.dutytracker.infrastructure.config.AppProperties;
import com.github.marcelorodrigo.dutytracker.usecase.calendarfeed.PreviewCalendarFeedUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.calendarfeed.GetCalendarFeedPreviewRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.calendarfeed.CalendarFeedEventResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.calendarfeed.CalendarFeedPreviewResponse;
import java.time.LocalDateTime;
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

@WebMvcTest(CalendarFeedController.class)
@Import(GlobalExceptionHandler.class)
@EnableConfigurationProperties(AppProperties.class)
class CalendarFeedControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private PreviewCalendarFeedUseCase previewCalendarFeedUseCase;

    @Test
    @DisplayName("GET /api/v1/calendar-feed/preview returns preview")
    void shouldReturnPreview() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 9, 0);
        CalendarFeedPreviewResponse preview = new CalendarFeedPreviewResponse(
                List.of(new CalendarFeedEventResponse(start, end, "On-call")), List.of());

        given(previewCalendarFeedUseCase.execute(new GetCalendarFeedPreviewRequest()))
                .willReturn(preview);

        assertThat(mvc.get().uri("/api/v1/calendar-feed/preview"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CalendarFeedPreviewResponse.class)
                .satisfies(res -> assertThat(res.upcoming()).hasSize(1));

        verify(previewCalendarFeedUseCase).execute(new GetCalendarFeedPreviewRequest());
    }

    @Test
    @DisplayName("GET /api/v1/calendar-feed/preview returns 404 when feed not configured")
    void shouldReturnNotFoundWhenNotConfigured() {
        given(previewCalendarFeedUseCase.execute(new GetCalendarFeedPreviewRequest()))
                .willThrow(
                        new com.github.marcelorodrigo.dutytracker.domain.exceptions
                                .CalendarFeedNotConfiguredException());

        assertThat(mvc.get().uri("/api/v1/calendar-feed/preview")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/v1/calendar-feed/preview returns 400 for invalid URL")
    void shouldReturnBadRequestForInvalidUrl() {
        given(previewCalendarFeedUseCase.execute(new GetCalendarFeedPreviewRequest()))
                .willThrow(new com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCalendarFeedUrlException(
                        "Bad URL"));

        assertThat(mvc.get().uri("/api/v1/calendar-feed/preview")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/calendar-feed/preview returns 422 when upstream rejects URL")
    void shouldReturnUnprocessableWhenAuthenticationFails() {
        given(previewCalendarFeedUseCase.execute(new GetCalendarFeedPreviewRequest()))
                .willThrow(
                        new com.github.marcelorodrigo.dutytracker.domain.exceptions
                                .CalendarFeedAuthenticationException());

        assertThat(mvc.get().uri("/api/v1/calendar-feed/preview")).hasStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("GET /api/v1/calendar-feed/preview returns 502 for fetch failure")
    void shouldReturnBadGatewayForFetchFailure() {
        given(previewCalendarFeedUseCase.execute(new GetCalendarFeedPreviewRequest()))
                .willThrow(new com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedFetchException(
                        "Network error"));

        assertThat(mvc.get().uri("/api/v1/calendar-feed/preview")).hasStatus(HttpStatus.BAD_GATEWAY);
    }
}
