package com.github.marcelorodrigo.dutytracker.usecase.response.calendarfeed;

import java.time.LocalDateTime;

public record CalendarFeedEventResponse(LocalDateTime startDateTime, LocalDateTime endDateTime, String summary) {}
