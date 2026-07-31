package com.github.marcelorodrigo.dutytracker.usecase.calendarfeed;

import com.github.marcelorodrigo.dutytracker.domain.CalendarFeedEvent;
import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedNotConfiguredException;
import com.github.marcelorodrigo.dutytracker.gateway.calendarfeed.CalendarFeedGateway;
import com.github.marcelorodrigo.dutytracker.gateway.calendarfeed.CalendarFeedParser;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.calendarfeed.GetCalendarFeedPreviewRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.calendarfeed.CalendarFeedEventResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.calendarfeed.CalendarFeedPreviewResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.calendarfeed.CalendarFeedUrlValidator;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreviewCalendarFeedUseCase implements UseCase<GetCalendarFeedPreviewRequest, CalendarFeedPreviewResponse> {

    public static final int MAX_EVENTS = 100;
    public static final int PAST_MONTHS = 12;

    private final EngineerProfileGateway profileGateway;
    private final CalendarFeedUrlValidator urlValidator;
    private final CalendarFeedGateway feedGateway;
    private final CalendarFeedParser feedParser;
    private final CalendarFeedEventResponseMapper calendarFeedEventMapper;
    private final Clock clock;

    @Override
    public CalendarFeedPreviewResponse execute(GetCalendarFeedPreviewRequest request) {
        String feedUrl = profileGateway
                .find()
                .map(EngineerProfile::calendarFeedUrl)
                .orElseThrow(CalendarFeedNotConfiguredException::new);

        urlValidator.validate(feedUrl);

        String icsData = feedGateway.fetch(feedUrl);
        List<CalendarFeedEvent> parsedEvents = feedParser.parse(icsData);

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime pastCutoff = now.minusMonths(PAST_MONTHS);

        List<CalendarFeedEventResponse> upcoming = parsedEvents.stream()
                .filter(event -> !event.startDateTime().isBefore(now))
                .sorted(Comparator.comparing(CalendarFeedEvent::startDateTime))
                .limit(MAX_EVENTS)
                .map(calendarFeedEventMapper::toResponse)
                .toList();

        List<CalendarFeedEventResponse> past = parsedEvents.stream()
                .filter(event -> event.startDateTime().isBefore(now)
                        && !event.startDateTime().isBefore(pastCutoff))
                .sorted(Comparator.comparing(CalendarFeedEvent::startDateTime).reversed())
                .limit(MAX_EVENTS)
                .map(calendarFeedEventMapper::toResponse)
                .toList();

        return new CalendarFeedPreviewResponse(upcoming, past);
    }
}
