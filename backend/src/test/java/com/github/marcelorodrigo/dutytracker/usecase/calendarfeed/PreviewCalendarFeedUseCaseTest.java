package com.github.marcelorodrigo.dutytracker.usecase.calendarfeed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.github.marcelorodrigo.dutytracker.domain.CalendarFeedEvent;
import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedAuthenticationException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedFetchException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedNotConfiguredException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedParseException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidCalendarFeedUrlException;
import com.github.marcelorodrigo.dutytracker.gateway.calendarfeed.CalendarFeedEventMapper;
import com.github.marcelorodrigo.dutytracker.gateway.calendarfeed.CalendarFeedGateway;
import com.github.marcelorodrigo.dutytracker.gateway.calendarfeed.CalendarFeedParser;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.request.calendarfeed.GetCalendarFeedPreviewRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.calendarfeed.CalendarFeedEventResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.calendarfeed.CalendarFeedPreviewResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.calendarfeed.CalendarFeedUrlValidator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PreviewCalendarFeedUseCaseTest {

    private static final String FEED_URL = "https://app.incident.io/feed.ics";

    @Mock
    private EngineerProfileGateway profileGateway;

    @Mock
    private CalendarFeedUrlValidator urlValidator;

    @Mock
    private CalendarFeedGateway feedGateway;

    @Mock
    private CalendarFeedParser feedParser;

    private final CalendarFeedEventMapper calendarFeedEventMapper =
            event -> new CalendarFeedEventResponse(event.startDateTime(), event.endDateTime(), event.summary());

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneId.of("UTC"));

    private PreviewCalendarFeedUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PreviewCalendarFeedUseCase(
                profileGateway, urlValidator, feedGateway, feedParser, calendarFeedEventMapper, clock);
    }

    private EngineerProfile profileWithFeed() {
        return new EngineerProfile(
                1L,
                Set.of(DayOfWeek.MONDAY),
                java.time.LocalTime.of(9, 0),
                java.time.LocalTime.of(17, 0),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(0.067),
                BigDecimal.valueOf(0.084),
                FEED_URL,
                LocalDateTime.now(clock));
    }

    @Test
    @DisplayName("should throw when profile has no calendar feed URL configured")
    void shouldThrowWhenNotConfigured() {
        given(profileGateway.find()).willReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetCalendarFeedPreviewRequest()))
                .isInstanceOf(CalendarFeedNotConfiguredException.class);

        verifyNoInteractions(urlValidator, feedGateway, feedParser);
    }

    @Test
    @DisplayName("should propagate invalid URL exception")
    void shouldPropagateInvalidUrlException() {
        given(profileGateway.find()).willReturn(Optional.of(profileWithFeed()));
        doThrow(new InvalidCalendarFeedUrlException("Bad URL"))
                .when(urlValidator)
                .validate(FEED_URL);

        assertThatThrownBy(() -> useCase.execute(new GetCalendarFeedPreviewRequest()))
                .isInstanceOf(InvalidCalendarFeedUrlException.class)
                .hasMessageContaining("Bad URL");
    }

    @Test
    @DisplayName("should split parsed events into upcoming and past")
    void shouldSplitEvents() {
        LocalDateTime now = LocalDateTime.now(clock);
        CalendarFeedEvent pastEvent = new CalendarFeedEvent("Past", now.minusDays(5), now.minusDays(4));
        CalendarFeedEvent upcomingEvent = new CalendarFeedEvent("Upcoming", now.plusDays(1), now.plusDays(2));
        CalendarFeedEvent tooOldEvent = new CalendarFeedEvent(
                "Too old", now.minusMonths(15), now.minusMonths(15).plusHours(1));

        given(profileGateway.find()).willReturn(Optional.of(profileWithFeed()));
        given(feedGateway.fetch(FEED_URL)).willReturn("ICS-DATA");
        given(feedParser.parse("ICS-DATA")).willReturn(List.of(upcomingEvent, pastEvent, tooOldEvent));

        CalendarFeedPreviewResponse response = useCase.execute(new GetCalendarFeedPreviewRequest());

        assertThat(response.upcoming()).hasSize(1);
        assertThat(response.upcoming().get(0))
                .extracting(CalendarFeedEventResponse::summary)
                .isEqualTo("Upcoming");
        assertThat(response.past()).hasSize(1);
        assertThat(response.past().get(0))
                .extracting(CalendarFeedEventResponse::summary)
                .isEqualTo("Past");
        verify(urlValidator).validate(FEED_URL);
        verify(feedGateway).fetch(FEED_URL);
        verify(feedParser).parse("ICS-DATA");
    }

    @Test
    @DisplayName("should propagate feed authentication exceptions")
    void shouldPropagateAuthenticationException() {
        given(profileGateway.find()).willReturn(Optional.of(profileWithFeed()));
        given(feedGateway.fetch(FEED_URL)).willThrow(new CalendarFeedAuthenticationException());

        assertThatThrownBy(() -> useCase.execute(new GetCalendarFeedPreviewRequest()))
                .isInstanceOf(CalendarFeedAuthenticationException.class);
    }

    @Test
    @DisplayName("should propagate feed fetch exceptions")
    void shouldPropagateFetchException() {
        given(profileGateway.find()).willReturn(Optional.of(profileWithFeed()));
        given(feedGateway.fetch(FEED_URL)).willThrow(new CalendarFeedFetchException("Network error"));

        assertThatThrownBy(() -> useCase.execute(new GetCalendarFeedPreviewRequest()))
                .isInstanceOf(CalendarFeedFetchException.class)
                .hasMessageContaining("Network error");
    }

    @Test
    @DisplayName("should propagate feed parse exceptions")
    void shouldPropagateParseException() {
        given(profileGateway.find()).willReturn(Optional.of(profileWithFeed()));
        given(feedGateway.fetch(FEED_URL)).willReturn("bad data");
        given(feedParser.parse(any())).willThrow(new CalendarFeedParseException("broken", new RuntimeException()));

        assertThatThrownBy(() -> useCase.execute(new GetCalendarFeedPreviewRequest()))
                .isInstanceOf(CalendarFeedParseException.class)
                .hasMessageContaining("broken");
    }
}
