package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import com.github.marcelorodrigo.dutytracker.domain.CalendarFeedEvent;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.CalendarFeedParseException;
import java.io.StringReader;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.transform.recurrence.Frequency;
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
            for (var component : calendar.getComponents()) {
                if (component instanceof VEvent vevent) {
                    List<CalendarFeedEvent> occurrences = parseEvent(vevent);
                    if (events.size() + occurrences.size() > MAX_PARSED_EVENTS) {
                        int remaining = MAX_PARSED_EVENTS - events.size();
                        events.addAll(occurrences.subList(0, remaining));
                        break;
                    }
                    events.addAll(occurrences);
                }
            }
            return events;
        } catch (ParserException e) {
            throw new CalendarFeedParseException("Failed to parse calendar feed: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            throw new CalendarFeedParseException("Failed to read calendar feed: " + e.getMessage(), e);
        }
    }

    private List<CalendarFeedEvent> parseEvent(VEvent vevent) {
        DtStart<?> dtStart = vevent.getStartDate().orElse(null);
        if (dtStart == null || dtStart.getDate() == null) {
            return List.of();
        }
        if (isDateValue(dtStart)) {
            return List.of();
        }

        Summary summary = vevent.getSummary();
        String summaryText = summary != null ? summary.getValue() : "";

        if (hasHighFrequencyRecurrence(vevent)) {
            return List.of();
        }

        boolean floating = dtStart.getDate() instanceof LocalDateTime;

        List<CalendarFeedEvent> occurrences = new ArrayList<>();
        Period<? extends Temporal> recurrenceWindow = buildRecurrenceWindow(floating);
        Set<? extends Period<?>> periods = vevent.calculateRecurrenceSet(recurrenceWindow);
        for (Period<?> period : periods) {
            Temporal start = period.getStart();
            Temporal end = period.getEnd();
            occurrences.add(new CalendarFeedEvent(
                    summaryText, toBusinessLocalDateTime(start, floating), toBusinessLocalDateTime(end, floating)));
        }
        return occurrences;
    }

    private boolean isDateValue(DtStart<?> dtStart) {
        Temporal temporal = dtStart.getDate();
        if (temporal instanceof LocalDate) {
            return true;
        }
        Optional<Parameter> valueParam = dtStart.getParameter(Parameter.VALUE);
        return valueParam.isPresent()
                && "DATE".equalsIgnoreCase(valueParam.get().getValue());
    }

    private boolean hasHighFrequencyRecurrence(VEvent vevent) {
        Optional<Property> property = vevent.getProperty(Property.RRULE);
        if (property.isEmpty() || !(property.get() instanceof RRule rrule)) {
            return false;
        }
        Frequency frequency = rrule.getRecur().getFrequency();
        return frequency == Frequency.SECONDLY || frequency == Frequency.MINUTELY || frequency == Frequency.HOURLY;
    }

    private Period<? extends Temporal> buildRecurrenceWindow(boolean floating) {
        if (floating) {
            LocalDateTime now = LocalDateTime.now(clock);
            return new Period<LocalDateTime>(
                    now.minusYears(RECURRENCE_YEARS_BACK), now.plusYears(RECURRENCE_YEARS_FORWARD));
        }
        ZonedDateTime now = Instant.now(clock).atZone(clock.getZone());
        return new Period<Instant>(
                now.minusYears(RECURRENCE_YEARS_BACK).toInstant(),
                now.plusYears(RECURRENCE_YEARS_FORWARD).toInstant());
    }

    private LocalDateTime toBusinessLocalDateTime(Temporal temporal, boolean floating) {
        if (floating) {
            if (temporal instanceof LocalDateTime localDateTime) {
                return localDateTime.truncatedTo(ChronoUnit.SECONDS);
            }
            String value = temporal.toString();
            if (value.endsWith("Z")) {
                value = value.substring(0, value.length() - 1);
            }
            return LocalDateTime.parse(value, FLOATING_FORMATTER).truncatedTo(ChronoUnit.SECONDS);
        }
        Instant instant = toInstant(temporal);
        return instant.atZone(clock.getZone()).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS);
    }

    private Instant toInstant(Temporal temporal) {
        if (temporal instanceof Instant instant) {
            return instant;
        }
        if (temporal instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (temporal instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.toInstant();
        }
        if (temporal instanceof LocalDateTime localDateTime) {
            return localDateTime.atZone(clock.getZone()).toInstant();
        }
        throw new CalendarFeedParseException(
                "Unsupported date-time value: " + temporal.getClass().getName(), null);
    }
}
