package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.Incident;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class OvertimeSegmentCalculator {

    private static final int MINUTES_PER_DAY = 24 * 60;

    List<TimeSegment> calculate(Incident incident, LocalTime workStart, LocalTime workEnd, boolean fullDayOvertime) {
        int incidentStartMinute = toMinutes(incident.startDateTime().toLocalTime());
        int incidentEndMinute = toMinutes(incident.endDateTime().toLocalTime());

        if (incidentEndMinute <= incidentStartMinute) {
            incidentEndMinute += MINUTES_PER_DAY;
        }

        if (fullDayOvertime) {
            return List.of(new TimeSegment(incidentStartMinute, incidentEndMinute));
        }

        int workStartMinute = toMinutes(workStart);
        int workEndMinute = toMinutes(workEnd);
        List<TimeSegment> segments = new ArrayList<>();

        if (incidentStartMinute < workStartMinute) {
            int segmentEnd = Math.min(workStartMinute, incidentEndMinute);
            if (segmentEnd > incidentStartMinute) {
                segments.add(new TimeSegment(incidentStartMinute, segmentEnd));
            }
        }

        if (incidentEndMinute > workEndMinute) {
            int segmentStart = Math.max(workEndMinute, incidentStartMinute);
            if (incidentEndMinute > segmentStart) {
                segments.add(new TimeSegment(segmentStart, incidentEndMinute));
            }
        }

        return List.copyOf(segments);
    }

    List<TimeSegment> splitAtMidnight(TimeSegment segment) {
        List<TimeSegment> result = new ArrayList<>();
        int currentMinute = segment.startMinute();

        while (currentMinute < segment.endMinute()) {
            int nextMidnight = ((currentMinute / MINUTES_PER_DAY) + 1) * MINUTES_PER_DAY;
            int segmentEnd = Math.min(nextMidnight, segment.endMinute());
            result.add(new TimeSegment(currentMinute, segmentEnd));
            currentMinute = segmentEnd;
        }

        return List.copyOf(result);
    }

    static int toMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }
}
