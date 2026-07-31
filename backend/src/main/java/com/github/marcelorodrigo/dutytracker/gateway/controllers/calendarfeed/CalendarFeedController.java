package com.github.marcelorodrigo.dutytracker.gateway.controllers.calendarfeed;

import com.github.marcelorodrigo.dutytracker.gateway.api.CalendarFeedApi;
import com.github.marcelorodrigo.dutytracker.usecase.calendarfeed.PreviewCalendarFeedUseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.calendarfeed.GetCalendarFeedPreviewRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.calendarfeed.CalendarFeedPreviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CalendarFeedController implements CalendarFeedApi {

    private final PreviewCalendarFeedUseCase previewCalendarFeed;

    @Override
    public ResponseEntity<CalendarFeedPreviewResponse> getCalendarFeedPreview() {
        var response = previewCalendarFeed.execute(new GetCalendarFeedPreviewRequest());
        log.atInfo().log("Calendar feed preview retrieved");
        return ResponseEntity.ok(response);
    }
}
