package com.github.marcelorodrigo.dutytracker.usecase.response.calendarfeed;

import java.util.List;

public record CalendarFeedPreviewResponse(
        List<CalendarFeedEventResponse> upcoming, List<CalendarFeedEventResponse> past) {}
