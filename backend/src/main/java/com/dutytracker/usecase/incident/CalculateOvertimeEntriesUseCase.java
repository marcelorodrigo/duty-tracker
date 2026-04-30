package com.dutytracker.usecase.incident;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;
import com.dutytracker.gateway.compensation.CompensationRateGateway;
import com.dutytracker.gateway.incident.IncidentGateway;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.incident.*;
import com.dutytracker.usecase.response.incident.*;
import com.dutytracker.usecase.validator.incident.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateOvertimeEntriesUseCase
        implements UseCase<CalculateOvertimeEntriesRequest, OvertimeEntriesResponse> {

    private static final LocalTime DEFAULT_WORK_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_WORK_END = LocalTime.of(17, 0);

    private final IncidentGateway incidentGateway;
    private final EngineerProfileGateway engineerProfileGateway;
    private final CompensationRateGateway compensationRateGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final CalculateOvertimeEntriesValidator validator;

    @Override
    public OvertimeEntriesResponse execute(CalculateOvertimeEntriesRequest request) {
        validator.validate(request);

        // STEP 1: Load incident
        Long incidentId = request.incidentId();
        Incident incident = incidentGateway
                .findById(incidentId)
                .orElseThrow(() -> new InvalidIncidentException("Incident not found: " + incidentId));

        // STEP 2: Load EngineerProfile (use defaults if absent)
        Optional<EngineerProfile> profileOpt = engineerProfileGateway.find();
        LocalTime workStart = profileOpt.map(EngineerProfile::workStartTime).orElse(DEFAULT_WORK_START);
        LocalTime workEnd = profileOpt.map(EngineerProfile::workEndTime).orElse(DEFAULT_WORK_END);

        // STEP 3: Determine if date is a holiday (stored holiday override or Sunday)
        Set<LocalDate> holidayOverrideDates = incident.onCallPeriodId() != null
                ? holidayOverrideGateway.findByOnCallPeriodId(incident.onCallPeriodId()).stream()
                        .map(HolidayOverride::date)
                        .collect(Collectors.toSet())
                : Set.of();

        boolean isHoliday =
                incident.date().getDayOfWeek() == DayOfWeek.SUNDAY || holidayOverrideDates.contains(incident.date());

        // Determine OvertimeDayType for allowance rate lookup
        OvertimeDayType overtimeDayType;
        if (isHoliday) {
            overtimeDayType = OvertimeDayType.SUNDAY_HOLIDAY;
        } else if (incident.date().getDayOfWeek() == DayOfWeek.SATURDAY) {
            overtimeDayType = OvertimeDayType.SATURDAY;
        } else {
            overtimeDayType = OvertimeDayType.WEEKDAY;
        }

        // STEP 4: Determine overtime segments
        List<int[]> segments = computeOvertimeSegments(incident, workStart, workEnd, isHoliday);

        if (segments.isEmpty()) {
            throw new IncidentDuringWorkingHoursException();
        }

        // STEP 5: Load OVERTIME_ALLOWANCE rates for the specific day type
        List<CompensationRate> allowanceRates = compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                RateCategory.OVERTIME_ALLOWANCE, overtimeDayType);

        // Build OvertimeEntry list from segments
        List<OvertimeEntry> entries = new ArrayList<>();
        for (int[] segment : segments) {
            buildEntriesForSegment(incidentId, segment[0], segment[1], allowanceRates, entries);
        }

        // STEP 6: Map to response
        List<OvertimeEntryResponse> responses = entries.stream()
                .map(e -> new OvertimeEntryResponse(
                        e.incidentId(),
                        e.overtimeHours(),
                        e.allowanceHours(),
                        e.allowancePercentage(),
                        e.timeFrom(),
                        e.timeTo(),
                        e.isAllowanceEntry()))
                .toList();

        return new OvertimeEntriesResponse(incidentId, responses);
    }

    /**
     * Returns overtime segments as [startMinutes, endMinutes] pairs (minutes from midnight).
     * For holidays/Sundays, the entire incident is overtime. For weekdays, working hours are excluded.
     */
    private List<int[]> computeOvertimeSegments(
            Incident incident, LocalTime workStart, LocalTime workEnd, boolean isHoliday) {
        int incidentStartMin = toMinutes(incident.startTime());
        int incidentEndMin = toMinutes(incident.endTime());

        // Handle overnight: if end <= start, treat end as next-day by adding 24h worth of minutes
        if (incidentEndMin <= incidentStartMin) {
            incidentEndMin += 24 * 60;
        }

        if (isHoliday) {
            return List.of(new int[] {incidentStartMin, incidentEndMin});
        }

        int workStartMin = toMinutes(workStart);
        int workEndMin = toMinutes(workEnd);

        List<int[]> segments = new ArrayList<>();

        // Segment before working hours
        if (incidentStartMin < workStartMin) {
            int segEnd = Math.min(workStartMin, incidentEndMin);
            if (segEnd > incidentStartMin) {
                segments.add(new int[] {incidentStartMin, segEnd});
            }
        }

        // Segment after working hours
        if (incidentEndMin > workEndMin) {
            int segStart = Math.max(workEndMin, incidentStartMin);
            if (incidentEndMin > segStart) {
                segments.add(new int[] {segStart, incidentEndMin});
            }
        }

        return segments;
    }

    /**
     * Splits a segment by OVERTIME_ALLOWANCE rate zone boundaries and builds OvertimeEntry records.
     * Segment boundaries are in minutes-from-midnight (may exceed 1440 for overnight).
     *
     * To handle overnight segments and rates correctly:
     * 1. Normalize the segment to [0, 1440) by computing relative minute positions
     * 2. For each rate zone, check intersection in the normalized space
     * 3. Track coverage with indices relative to segment start, not absolute minutes
     */
    private void buildEntriesForSegment(
            Long incidentId,
            int segFromMin,
            int segToMin,
            List<CompensationRate> allowanceRates,
            List<OvertimeEntry> entries) {

        // Collect sub-segments: portions of [segFromMin, segToMin] that fall within each rate zone,
        // plus any uncovered remainder.
        List<int[]> subSegments = new ArrayList<>();
        int segmentDurationMinutes = segToMin - segFromMin;
        boolean[] covered = new boolean[segmentDurationMinutes]; // one slot per minute

        for (CompensationRate rate : allowanceRates) {
            int rateFromMin = toMinutes(rate.timeFrom());
            int rateToMin = toMinutes(rate.timeTo());

            // Handle overnight rate zones (e.g. 22:00 – 00:00 where timeTo == 00:00 means midnight)
            if (rateToMin <= rateFromMin) {
                rateToMin += 24 * 60;
            }

            // Intersect rate zone with segment, handling overnight wrapping
            int overlapFrom = Math.max(segFromMin, rateFromMin);
            int overlapTo = Math.min(segToMin, rateToMin);

            // Also handle when segment is overnight and rate zone is in the "next day" part
            if (overlapFrom >= overlapTo) {
                // Try shifting rate zone by 24h
                overlapFrom = Math.max(segFromMin, rateFromMin + 24 * 60);
                overlapTo = Math.min(segToMin, rateToMin + 24 * 60);
            }

            if (overlapFrom < overlapTo) {
                subSegments.add(new int[] {overlapFrom, overlapTo, rateIndex(allowanceRates, rate)});
                // Mark covered minutes relative to segment start
                for (int m = overlapFrom - segFromMin; m < overlapTo - segFromMin; m++) {
                    if (m >= 0 && m < covered.length) covered[m] = true;
                }
            }
        }

        // Add uncovered portions as sub-segments with rateIndex = -1 (no allowance)
        int rangeStart = -1;
        for (int i = 0; i <= covered.length; i++) {
            boolean inGap = i < covered.length && !covered[i];
            if (inGap && rangeStart == -1) {
                rangeStart = i;
            } else if (!inGap && rangeStart != -1) {
                subSegments.add(new int[] {segFromMin + rangeStart, segFromMin + i, -1});
                rangeStart = -1;
            }
        }

        // Build OvertimeEntry records for each sub-segment
        for (int[] sub : subSegments) {
            int subFromMin = sub[0];
            int subToMin = sub[1];
            int rateIdx = sub[2];

            int durationMinutes = subToMin - subFromMin;
            int roundedHours = Math.max(1, (int) Math.ceil(durationMinutes / 60.0));
            BigDecimal hoursDecimal = BigDecimal.valueOf(roundedHours).setScale(4, RoundingMode.UNNECESSARY);

            // Normalize times back to [0, 1440)
            LocalTime fromTime = fromMinutes(subFromMin % (24 * 60));
            LocalTime toTime = fromMinutes(subToMin % (24 * 60));

            // Base entry
            entries.add(new OvertimeEntry(incidentId, hoursDecimal, null, null, fromTime, toTime, false));

            // Allowance entry (only when a matching rate zone was found and percentage > 0%)
            if (rateIdx >= 0) {
                CompensationRate rate = allowanceRates.get(rateIdx);
                if (rate.percentage().compareTo(BigDecimal.ZERO) > 0) {
                    entries.add(new OvertimeEntry(
                            incidentId, null, hoursDecimal, rate.percentage(), fromTime, toTime, true));
                }
            }
        }
    }

    private int rateIndex(List<CompensationRate> rates, CompensationRate target) {
        for (int i = 0; i < rates.size(); i++) {
            if (rates.get(i) == target) return i;
        }
        return -1;
    }

    private static int toMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private static LocalTime fromMinutes(int minutes) {
        return LocalTime.of((minutes / 60) % 24, minutes % 60);
    }
}
