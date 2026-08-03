package com.github.marcelorodrigo.dutytracker.usecase.calendarfeed;

import com.github.marcelorodrigo.dutytracker.domain.CalendarFeedEvent;
import com.github.marcelorodrigo.dutytracker.usecase.response.calendarfeed.CalendarFeedEventResponse;

public interface CalendarFeedEventResponseMapper {

    CalendarFeedEventResponse toResponse(CalendarFeedEvent event);
}
