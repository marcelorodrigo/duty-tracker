package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.marcelorodrigo.dutytracker.domain.CalendarFeedEvent;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Ical4jCalendarFeedParserTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneId.of("Europe/Amsterdam"));

    private final Ical4jCalendarFeedParser parser = new Ical4jCalendarFeedParser(CLOCK);

    @Test
    @DisplayName("should parse a single UTC VEVENT")
    void shouldParseUtcEvent() {
        String ics = """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//DutyTracker//Test//EN
                BEGIN:VEVENT
                UID:test-1
                DTSTART:20260110T080000Z
                DTEND:20260117T080000Z
                SUMMARY:On-call
                END:VEVENT
                END:VCALENDAR
                """;

        List<CalendarFeedEvent> events = parser.parse(ics);

        assertThat(events).hasSize(1);
        CalendarFeedEvent event = events.get(0);
        assertThat(event.summary()).isEqualTo("On-call");
        // Input is UTC; output is converted to the configured business zone (Europe/Amsterdam, UTC+1 in January).
        assertThat(event.startDateTime()).isEqualTo(LocalDateTime.of(2026, 1, 10, 9, 0));
        assertThat(event.endDateTime()).isEqualTo(LocalDateTime.of(2026, 1, 17, 9, 0));
    }

    @Test
    @DisplayName("should parse a floating VEVENT using business zone")
    void shouldParseFloatingEvent() {
        String ics = """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//DutyTracker//Test//EN
                BEGIN:VEVENT
                UID:test-2
                DTSTART:20260110T090000
                DTEND:20260110T170000
                SUMMARY:Floating
                END:VEVENT
                END:VCALENDAR
                """;

        List<CalendarFeedEvent> events = parser.parse(ics);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).startDateTime()).isEqualTo(LocalDateTime.of(2026, 1, 10, 9, 0));
    }

    @Test
    @DisplayName("should expand recurring daily event within range")
    void shouldExpandRecurringDailyEvent() {
        String ics = """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//DutyTracker//Test//EN
                BEGIN:VEVENT
                UID:test-3
                DTSTART:20260110T080000Z
                DTEND:20260111T080000Z
                RRULE:FREQ=DAILY;COUNT=3
                SUMMARY:Recurring
                END:VEVENT
                END:VCALENDAR
                """;

        List<CalendarFeedEvent> events = parser.parse(ics);

        assertThat(events).hasSize(3);
        assertThat(events).allMatch(e -> e.summary().equals("Recurring"));
    }

    @Test
    @DisplayName("should throw parse exception for invalid ICS data")
    void shouldThrowOnInvalidData() {
        assertThatThrownBy(() -> parser.parse("not a calendar"))
                .isInstanceOf(CalendarFeedParseException.class)
                .hasMessageContaining("parse");
    }

    @Test
    @DisplayName("should return empty list for valid ICS with no VEVENT")
    void shouldReturnEmptyListForEmptyCalendar() {
        String ics = """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//DutyTracker//Test//EN
                END:VCALENDAR
                """;

        assertThat(parser.parse(ics)).isEmpty();
    }

    @Test
    @DisplayName("should ignore all-day VEVENT with VALUE=DATE")
    void shouldIgnoreAllDayEvent() {
        String ics = """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//DutyTracker//Test//EN
                BEGIN:VEVENT
                UID:test-allday
                DTSTART;VALUE=DATE:20260110
                DTEND;VALUE=DATE:20260111
                SUMMARY:All-day on-call
                END:VEVENT
                END:VCALENDAR
                """;

        assertThat(parser.parse(ics)).isEmpty();
    }
}
