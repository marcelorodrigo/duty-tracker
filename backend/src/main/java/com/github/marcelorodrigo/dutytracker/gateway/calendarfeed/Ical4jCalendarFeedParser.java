package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import com.github.marcelorodrigo.dutytracker.domain.CalendarFeedEvent;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedParseException;
import java.io.StringReader;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Summary;
import org.springframework.stereotype.Component;

@Component
public class Ical4jCalendarFeedParser implements CalendarFeedParser {

    private static final int RECURRENCE_YEARS_BACK = 1;
    private static final int RECURRENCE_YEARS_FORWARD = 10;
    private static final int MAX_PARSED_EVENTS = 1000;
    private static final DateTimeFormatter FLOATING_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private final Clock clock;

    public Ical4jCalendarFeedParser(Clock clock) {
        this.clock = clock;
    }

    @Override
    public List<CalendarFeedEvent> parse(String icsData) {
        try {
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(new StringReader(icsData));
            List<CalendarFeedEvent> events = new ArrayList<>();
            for (Object component : calendar.getComponents("VEVENT")) {
                VEvent vevent = (VEvent) component;
                List<CalendarFeedEvent> occurrences = parseEvent(vevent);
                if (events.size() + occurrences.size() > MAX_PARSED_EVENTS) {
                    int remaining = MAX_PARSED_EVENTS - events.size();
                    events.addAll(occurrences.subList(0, remaining));
                    break;
                }
                events.addAll(occurrences);
            }
            return events;
        } catch (ParserException e) {
            throw new CalendarFeedParseException("Failed to parse calendar feed: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            throw new CalendarFeedParseException("Failed to read calendar feed: " + e.getMessage(), e);
        }
    }

    private List<CalendarFeedEvent> parseEvent(VEvent vevent) {
        DtStart dtStart = vevent.getStartDate();
        DtEnd dtEnd = vevent.getEndDate(true);
        if (dtStart == null || dtStart.getDate() == null) {
            return List.of();
        }
        if (isDateValue(dtStart)) {
            return List.of();
        }

        Summary summary = vevent.getSummary();
        String summaryText = summary != null ? summary.getValue() : "";

        TimeInterpretation mode = resolveTimeInterpretation(dtStart);

        List<CalendarFeedEvent> occurrences = new ArrayList<>();
        Period recurrenceWindow = buildRecurrenceWindow();
        @SuppressWarnings("unchecked")
        net.fortuna.ical4j.model.PeriodList periods = vevent.calculateRecurrenceSet(recurrenceWindow);
        for (Object periodObj : periods) {
            Period period = (Period) periodObj;
            DateTime start = period.getStart();
            DateTime end = period.getEnd();
            occurrences.add(new CalendarFeedEvent(
                    summaryText, toBusinessLocalDateTime(start, mode), toBusinessLocalDateTime(end, mode)));
        }
        return occurrences;
    }

    private boolean isDateValue(DateProperty property) {
        Parameter valueParam = property.getParameter(Parameter.VALUE);
        if (valueParam != null && "DATE".equalsIgnoreCase(valueParam.getValue())) {
            return true;
        }
        return property.getDate() instanceof Date && !(property.getDate() instanceof DateTime);
    }

    private TimeInterpretation resolveTimeInterpretation(DtStart dtStart) {
        DateTime dateTime = (DateTime) dtStart.getDate();
        if (dateTime.isUtc()) {
            return TimeInterpretation.UTC;
        }
        Parameter tzid = dtStart.getParameter(Parameter.TZID);
        if (tzid != null && !tzid.getValue().isBlank()) {
            return TimeInterpretation.ZONED;
        }
        return TimeInterpretation.FLOATING;
    }

    private enum TimeInterpretation {
        UTC,
        ZONED,
        FLOATING
    }

    private Period buildRecurrenceWindow() {
        LocalDateTime now = LocalDateTime.now(clock);
        DateTime from = toIcalDateTime(now.minusYears(RECURRENCE_YEARS_BACK));
        DateTime to = toIcalDateTime(now.plusYears(RECURRENCE_YEARS_FORWARD));
        return new Period(from, to);
    }

    private DateTime toIcalDateTime(LocalDateTime localDateTime) {
        return new DateTime(
                java.util.Date.from(localDateTime.atZone(clock.getZone()).toInstant()));
    }

    private LocalDateTime toBusinessLocalDateTime(DateTime dateTime, TimeInterpretation mode) {
        if (mode == TimeInterpretation.FLOATING) {
            String value = dateTime.toString();
            if (value.endsWith("Z")) {
                value = value.substring(0, value.length() - 1);
            }
            return LocalDateTime.parse(value, FLOATING_FORMATTER).truncatedTo(ChronoUnit.SECONDS);
        }
        return Instant.ofEpochMilli(dateTime.getTime()).atZone(clock.getZone()).toLocalDateTime();
    }
}
