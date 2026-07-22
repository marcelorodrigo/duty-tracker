package com.github.marcelorodrigo.dutytracker.usecase.incident;

import com.github.marcelorodrigo.dutytracker.domain.CompensationRate;
import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.Incident;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeDayType;
import com.github.marcelorodrigo.dutytracker.domain.OvertimeEntry;
import com.github.marcelorodrigo.dutytracker.domain.RateCategory;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.IncidentDuringWorkingHoursException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidIncidentException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.compensation.CompensationRateGateway;
import com.github.marcelorodrigo.dutytracker.gateway.incident.IncidentGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.incident.CalculateOvertimeEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.incident.OvertimeEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.incident.CalculateOvertimeEntriesValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalculateOvertimeEntriesUseCase
        implements UseCase<CalculateOvertimeEntriesRequest, OvertimeEntriesResponse> {

    private static final int MINUTES_PER_HOUR = 60;

    private final IncidentGateway incidentGateway;
    private final EngineerProfileGateway engineerProfileGateway;
    private final CompensationRateGateway compensationRateGateway;
    private final HolidayGateway holidayGateway;
    private final CalculateOvertimeEntriesValidator validator;

    @Override
    @Transactional(readOnly = true)
    public OvertimeEntriesResponse execute(CalculateOvertimeEntriesRequest request) {
        validator.validate(request);

        // STEP 1: Load incident
        Long incidentId = request.incidentId();
        Incident incident = incidentGateway
                .findById(incidentId)
                .orElseThrow(() -> new InvalidIncidentException("Incident not found: " + incidentId));

        // STEP 2: Load EngineerProfile
        EngineerProfile profile = engineerProfileGateway
                .find()
                .orElseThrow(() -> new ProfileNotFoundException("EngineerProfile not found"));
        LocalTime workStart = profile.workStartTime();
        LocalTime workEnd = profile.workEndTime();

        // STEP 3: Determine if date is a holiday (stored holiday override or Sunday)
        LocalDate incidentDate = incident.startDateTime().toLocalDate();
        Set<LocalDate> holidayDates = holidayGateway.findByOnCallPeriodId(incident.onCallPeriodId()).stream()
                .map(Holiday::date)
                .collect(Collectors.toSet());

        boolean isHoliday = incidentDate.getDayOfWeek() == DayOfWeek.SUNDAY || holidayDates.contains(incidentDate);

        // Determine OvertimeDayType for allowance rate lookup
        OvertimeDayType overtimeDayType;
        if (isHoliday) {
            overtimeDayType = OvertimeDayType.SUNDAY_HOLIDAY;
        } else if (incidentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            overtimeDayType = OvertimeDayType.SATURDAY;
        } else {
            overtimeDayType = OvertimeDayType.WEEKDAY;
        }

        // STEP 4: Determine overtime segments
        List<TimeSegment> segments = computeOvertimeSegments(incident, workStart, workEnd, isHoliday);

        if (segments.isEmpty()) {
            throw new IncidentDuringWorkingHoursException();
        }

        // STEP 5: Load OVERTIME_ALLOWANCE rates for the specific day type
        List<CompensationRate> allowanceRates = compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                RateCategory.OVERTIME_ALLOWANCE, overtimeDayType);

        // Build OvertimeEntry list from segments
        List<OvertimeEntry> entries = new ArrayList<>();
        for (TimeSegment segment : segments) {
            for (TimeSegment daySegment : splitAtMidnight(segment)) {
                buildEntriesForSegment(incidentId, incidentDate, daySegment, allowanceRates, entries);
            }
        }

        // STEP 6: Map to response
        List<OvertimeEntryResponse> responses = entries.stream()
                .map(e -> new OvertimeEntryResponse(
                        e.incidentId(),
                        e.overtimeHours(),
                        e.allowanceHours(),
                        e.allowancePercentage(),
                        e.date(),
                        e.timeFrom(),
                        e.timeTo(),
                        e.isAllowanceEntry()))
                .toList();

        return new OvertimeEntriesResponse(incidentId, responses);
    }

    private List<TimeSegment> computeOvertimeSegments(
            Incident incident, LocalTime workStart, LocalTime workEnd, boolean isHoliday) {
        int incidentStartMin = toMinutes(incident.startDateTime().toLocalTime());
        int incidentEndMin = toMinutes(incident.endDateTime().toLocalTime());

        if (incidentEndMin <= incidentStartMin) {
            incidentEndMin += 24 * 60;
        }

        if (isHoliday) {
            return List.of(new TimeSegment(incidentStartMin, incidentEndMin));
        }

        int workStartMin = toMinutes(workStart);
        int workEndMin = toMinutes(workEnd);

        List<TimeSegment> segments = new ArrayList<>();

        if (incidentStartMin < workStartMin) {
            int segEnd = Math.min(workStartMin, incidentEndMin);
            if (segEnd > incidentStartMin) {
                segments.add(new TimeSegment(incidentStartMin, segEnd));
            }
        }

        if (incidentEndMin > workEndMin) {
            int segStart = Math.max(workEndMin, incidentStartMin);
            if (incidentEndMin > segStart) {
                segments.add(new TimeSegment(segStart, incidentEndMin));
            }
        }

        return segments;
    }

    private void buildEntriesForSegment(
            Long incidentId,
            LocalDate incidentDate,
            TimeSegment segment,
            List<CompensationRate> allowanceRates,
            List<OvertimeEntry> entries) {

        List<RatedTimeSegment> subSegments = computeSubSegments(segment, allowanceRates);

        for (RatedTimeSegment subSegment : subSegments) {
            appendOvertimeEntry(incidentId, incidentDate, entries, subSegment);
        }
    }

    private List<RatedTimeSegment> computeSubSegments(TimeSegment segment, List<CompensationRate> allowanceRates) {
        int durationMinutes = segment.endMinute() - segment.startMinute();
        boolean[] covered = new boolean[durationMinutes];
        List<RatedTimeSegment> subSegments = new ArrayList<>();

        appendRateSubSegments(segment, allowanceRates, subSegments, covered);
        appendGapSubSegments(segment.startMinute(), covered, subSegments);

        return subSegments;
    }

    private void appendRateSubSegments(
            TimeSegment segment,
            List<CompensationRate> allowanceRates,
            List<RatedTimeSegment> subSegments,
            boolean[] covered) {

        for (CompensationRate rate : allowanceRates) {
            int rateFromMin = toMinutes(rate.timeFrom());
            int rateToMin = toMinutes(rate.timeTo());

            if (rateToMin <= rateFromMin) {
                rateToMin += 24 * 60;
            }

            int overlapFrom = Math.max(segment.startMinute(), rateFromMin);
            int overlapTo = Math.min(segment.endMinute(), rateToMin);

            if (overlapFrom >= overlapTo) {
                overlapFrom = Math.max(segment.startMinute(), rateFromMin + 24 * 60);
                overlapTo = Math.min(segment.endMinute(), rateToMin + 24 * 60);
            }

            if (overlapFrom < overlapTo) {
                subSegments.add(new RatedTimeSegment(new TimeSegment(overlapFrom, overlapTo), rate));
                int markFrom = Math.max(0, overlapFrom - segment.startMinute());
                int markTo = Math.min(covered.length, overlapTo - segment.startMinute());
                Arrays.fill(covered, markFrom, markTo, true);
            }
        }
    }

    private void appendGapSubSegments(int segFromMin, boolean[] covered, List<RatedTimeSegment> subSegments) {
        int rangeStart = -1;
        for (int i = 0; i <= covered.length; i++) {
            boolean inGap = i < covered.length && !covered[i];
            if (inGap && rangeStart == -1) {
                rangeStart = i;
            } else if (!inGap && rangeStart != -1) {
                subSegments.add(new RatedTimeSegment(new TimeSegment(segFromMin + rangeStart, segFromMin + i), null));
                rangeStart = -1;
            }
        }
    }

    private void appendOvertimeEntry(
            Long incidentId, LocalDate incidentDate, List<OvertimeEntry> entries, RatedTimeSegment subSegment) {

        int subFromMin = subSegment.timeSegment().startMinute();
        int subToMin = subSegment.timeSegment().endMinute();

        int durationMinutes = subToMin - subFromMin;
        int roundedHours = Math.max(1, (durationMinutes + MINUTES_PER_HOUR - 1) / MINUTES_PER_HOUR);
        BigDecimal hoursDecimal = BigDecimal.valueOf(roundedHours).setScale(4, RoundingMode.UNNECESSARY);

        LocalDate subDate = incidentDate.plusDays(subFromMin / (24 * 60));
        LocalTime fromTime = fromMinutes(subFromMin % (24 * 60));
        LocalTime toTime = fromMinutes(subToMin % (24 * 60));

        entries.add(new OvertimeEntry(incidentId, hoursDecimal, null, null, subDate, fromTime, toTime, false));

        CompensationRate rate = subSegment.allowanceRate();
        if (rate != null) {
            if (rate.percentage().compareTo(BigDecimal.ZERO) > 0) {
                entries.add(new OvertimeEntry(
                        incidentId, null, hoursDecimal, rate.percentage(), subDate, fromTime, toTime, true));
            }
        }
    }

    static List<TimeSegment> splitAtMidnight(TimeSegment segment) {
        List<TimeSegment> result = new ArrayList<>();
        int current = segment.startMinute();
        while (current < segment.endMinute()) {
            int nextMidnight = ((current / (24 * 60)) + 1) * (24 * 60);
            int end = Math.min(nextMidnight, segment.endMinute());
            result.add(new TimeSegment(current, end));
            current = end;
        }
        return result;
    }

    private static int toMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private static LocalTime fromMinutes(int minutes) {
        return LocalTime.of((minutes / 60) % 24, minutes % 60);
    }

    private record RatedTimeSegment(TimeSegment timeSegment, CompensationRate allowanceRate) {}
}
