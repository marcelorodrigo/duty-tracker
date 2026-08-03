package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import com.github.marcelorodrigo.dutytracker.domain.CalendarFeedEvent;
import java.util.List;

public interface CalendarFeedParser {
    List<CalendarFeedEvent> parse(String icsData);
}
