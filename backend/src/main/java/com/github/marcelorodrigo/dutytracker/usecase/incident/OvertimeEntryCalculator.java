package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.Hours;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeEntry;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class OvertimeEntryCalculator {

    private static final int MINUTES_PER_DAY = 24 * 60;

    private final OvertimeSegmentCalculator segmentCalculator;

    OvertimeEntryCalculator(OvertimeSegmentCalculator segmentCalculator) {
        this.segmentCalculator = segmentCalculator;
    }

    List<OvertimeEntry> calculate(
            Long incidentId,
            LocalDate incidentDate,
            List<TimeSegment> segments,
            List<CompensationRate> allowanceRates) {
        List<OvertimeEntry> entries = new ArrayList<>();

        for (TimeSegment segment : segments) {
            for (TimeSegment daySegment : segmentCalculator.splitAtMidnight(segment)) {
                appendEntries(incidentId, incidentDate, daySegment, allowanceRates, entries);
            }
        }

        return List.copyOf(entries);
    }

    private void appendEntries(
            Long incidentId,
            LocalDate incidentDate,
            TimeSegment segment,
            List<CompensationRate> allowanceRates,
            List<OvertimeEntry> entries) {
        for (RatedTimeSegment ratedSegment : calculateRatedSegments(segment, allowanceRates)) {
            appendEntryPair(incidentId, incidentDate, ratedSegment, entries);
        }
    }

    private List<RatedTimeSegment> calculateRatedSegments(TimeSegment segment, List<CompensationRate> allowanceRates) {
        boolean[] coveredMinutes = new boolean[segment.endMinute() - segment.startMinute()];
        List<RatedTimeSegment> ratedSegments = new ArrayList<>();

        appendRateSegments(segment, allowanceRates, ratedSegments, coveredMinutes);
        appendGapSegments(segment.startMinute(), coveredMinutes, ratedSegments);

        return ratedSegments;
    }

    private void appendRateSegments(
            TimeSegment segment,
            List<CompensationRate> allowanceRates,
            List<RatedTimeSegment> ratedSegments,
            boolean[] coveredMinutes) {
        for (CompensationRate rate : allowanceRates) {
            int rateFromMinute = OvertimeSegmentCalculator.toMinutes(rate.timeFrom());
            int rateToMinute = OvertimeSegmentCalculator.toMinutes(rate.timeTo());

            if (rateToMinute <= rateFromMinute) {
                rateToMinute += MINUTES_PER_DAY;
            }

            int overlapFrom = Math.max(segment.startMinute(), rateFromMinute);
            int overlapTo = Math.min(segment.endMinute(), rateToMinute);

            if (overlapFrom >= overlapTo) {
                overlapFrom = Math.max(segment.startMinute(), rateFromMinute + MINUTES_PER_DAY);
                overlapTo = Math.min(segment.endMinute(), rateToMinute + MINUTES_PER_DAY);
            }

            if (overlapFrom < overlapTo) {
                ratedSegments.add(new RatedTimeSegment(new TimeSegment(overlapFrom, overlapTo), rate));
                int markFrom = Math.max(0, overlapFrom - segment.startMinute());
                int markTo = Math.min(coveredMinutes.length, overlapTo - segment.startMinute());
                Arrays.fill(coveredMinutes, markFrom, markTo, true);
            }
        }
    }

    private void appendGapSegments(
            int segmentStartMinute, boolean[] coveredMinutes, List<RatedTimeSegment> ratedSegments) {
        int rangeStart = -1;
        for (int minute = 0; minute <= coveredMinutes.length; minute++) {
            boolean inGap = minute < coveredMinutes.length && !coveredMinutes[minute];
            if (inGap && rangeStart == -1) {
                rangeStart = minute;
            } else if (!inGap && rangeStart != -1) {
                ratedSegments.add(new RatedTimeSegment(
                        new TimeSegment(segmentStartMinute + rangeStart, segmentStartMinute + minute), null));
                rangeStart = -1;
            }
        }
    }

    private void appendEntryPair(
            Long incidentId, LocalDate incidentDate, RatedTimeSegment ratedSegment, List<OvertimeEntry> entries) {
        int fromMinute = ratedSegment.timeSegment().startMinute();
        int toMinute = ratedSegment.timeSegment().endMinute();
        Hours roundedHours = Hours.roundedUpFromMinutes(toMinute - fromMinute);
        LocalDate date = incidentDate.plusDays(fromMinute / MINUTES_PER_DAY);
        LocalTime fromTime = fromMinutes(fromMinute % MINUTES_PER_DAY);
        LocalTime toTime = fromMinutes(toMinute % MINUTES_PER_DAY);

        entries.add(new OvertimeEntry(incidentId, roundedHours, null, null, date, fromTime, toTime, false));

        var rate = ratedSegment.allowanceRate();
        if (rate != null && rate.percentage().isPositive()) {
            entries.add(
                    new OvertimeEntry(incidentId, null, roundedHours, rate.percentage(), date, fromTime, toTime, true));
        }
    }

    private static LocalTime fromMinutes(int minutes) {
        return LocalTime.of((minutes / 60) % 24, minutes % 60);
    }

    private record RatedTimeSegment(TimeSegment timeSegment, CompensationRate allowanceRate) {}
}
