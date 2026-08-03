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
    private static final Set<DayOfWeek> DEFAULT_WORKING_DAYS =
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private final IncidentGateway incidentGateway;
    private final EngineerProfileGateway engineerProfileGateway;
    private final CompensationRateGateway compensationRateGateway;
    private final HolidayGateway holidayGateway;
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
        Set<DayOfWeek> workingDays =
                profileOpt.map(EngineerProfile::workingDays).orElse(DEFAULT_WORKING_DAYS);

        // STEP 3: Determine if date is a holiday (stored holiday override or Sunday)
        LocalDate incidentDate = incident.startDateTime().toLocalDate();
        Set<LocalDate> holidayDates = holidayGateway.findByOnCallPeriodId(incident.onCallPeriodId()).stream()
                .map(Holiday::date)
                .collect(Collectors.toSet());

        boolean isHoliday = incidentDate.getDayOfWeek() == DayOfWeek.SUNDAY || holidayDates.contains(incidentDate);
        boolean isWorkingDay = workingDays.contains(incidentDate.getDayOfWeek()) && !isHoliday;

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
        List<int[]> segments = computeOvertimeSegments(incident, workStart, workEnd, isWorkingDay);

        if (segments.isEmpty()) {
            throw new IncidentDuringWorkingHoursException();
        }

        // STEP 5: Load OVERTIME_ALLOWANCE rates for the specific day type
        List<CompensationRate> allowanceRates = compensationRateGateway.findByRateCategoryAndOvertimeDayType(
                RateCategory.OVERTIME_ALLOWANCE, overtimeDayType);

        // Build OvertimeEntry list from segments
        List<OvertimeEntry> entries = new ArrayList<>();
        for (int[] segment : segments) {
            for (int[] daySegment : splitAtMidnight(segment[0], segment[1])) {
                buildEntriesForSegment(incidentId, incidentDate, daySegment[0], daySegment[1], allowanceRates, entries);
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

    private List<int[]> computeOvertimeSegments(
            Incident incident, LocalTime workStart, LocalTime workEnd, boolean isWorkingDay) {
        int incidentStartMin = toMinutes(incident.startDateTime().toLocalTime());
        int incidentEndMin = toMinutes(incident.endDateTime().toLocalTime());

        if (incidentEndMin <= incidentStartMin) {
            incidentEndMin += 24 * 60;
        }

        if (!isWorkingDay) {
            return List.of(new int[] {incidentStartMin, incidentEndMin});
        }

        int workStartMin = toMinutes(workStart);
        int workEndMin = toMinutes(workEnd);

        List<int[]> segments = new ArrayList<>();

        if (incidentStartMin < workStartMin) {
            int segEnd = Math.min(workStartMin, incidentEndMin);
            if (segEnd > incidentStartMin) {
                segments.add(new int[] {incidentStartMin, segEnd});
            }
        }

        if (incidentEndMin > workEndMin) {
            int segStart = Math.max(workEndMin, incidentStartMin);
            if (incidentEndMin > segStart) {
                segments.add(new int[] {segStart, incidentEndMin});
            }
        }

        return segments;
    }

    private void buildEntriesForSegment(
            Long incidentId,
            LocalDate incidentDate,
            int segFromMin,
            int segToMin,
            List<CompensationRate> allowanceRates,
            List<OvertimeEntry> entries) {

        List<int[]> subSegments = computeSubSegments(segFromMin, segToMin, allowanceRates);

        for (int[] sub : subSegments) {
            appendOvertimeEntry(incidentId, incidentDate, allowanceRates, entries, sub);
        }
    }

    private List<int[]> computeSubSegments(int segFromMin, int segToMin, List<CompensationRate> allowanceRates) {
        int durationMinutes = segToMin - segFromMin;
        boolean[] covered = new boolean[durationMinutes];
        List<int[]> subSegments = new ArrayList<>();

        appendRateSubSegments(segFromMin, segToMin, allowanceRates, subSegments, covered);
        appendGapSubSegments(segFromMin, covered, subSegments);

        return subSegments;
    }

    private void appendRateSubSegments(
            int segFromMin,
            int segToMin,
            List<CompensationRate> allowanceRates,
            List<int[]> subSegments,
            boolean[] covered) {

        for (CompensationRate rate : allowanceRates) {
            int rateFromMin = toMinutes(rate.timeFrom());
            int rateToMin = toMinutes(rate.timeTo());

            if (rateToMin <= rateFromMin) {
                rateToMin += 24 * 60;
            }

            int overlapFrom = Math.max(segFromMin, rateFromMin);
            int overlapTo = Math.min(segToMin, rateToMin);

            if (overlapFrom >= overlapTo) {
                overlapFrom = Math.max(segFromMin, rateFromMin + 24 * 60);
                overlapTo = Math.min(segToMin, rateToMin + 24 * 60);
            }

            if (overlapFrom < overlapTo) {
                subSegments.add(new int[] {overlapFrom, overlapTo, allowanceRates.indexOf(rate)});
                int markFrom = Math.max(0, overlapFrom - segFromMin);
                int markTo = Math.min(covered.length, overlapTo - segFromMin);
                Arrays.fill(covered, markFrom, markTo, true);
            }
        }
    }

    private void appendGapSubSegments(int segFromMin, boolean[] covered, List<int[]> subSegments) {
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
    }

    private void appendOvertimeEntry(
            Long incidentId,
            LocalDate incidentDate,
            List<CompensationRate> allowanceRates,
            List<OvertimeEntry> entries,
            int[] sub) {

        int subFromMin = sub[0];
        int subToMin = sub[1];
        int rateIdx = sub[2];

        int durationMinutes = subToMin - subFromMin;
        int roundedHours = Math.max(1, (int) Math.ceil(durationMinutes / 60.0));
        BigDecimal hoursDecimal = BigDecimal.valueOf(roundedHours).setScale(4, RoundingMode.UNNECESSARY);

        LocalDate subDate = incidentDate.plusDays(subFromMin / (24 * 60));
        LocalTime fromTime = fromMinutes(subFromMin % (24 * 60));
        LocalTime toTime = fromMinutes(subToMin % (24 * 60));

        entries.add(new OvertimeEntry(incidentId, hoursDecimal, null, null, subDate, fromTime, toTime, false));

        if (rateIdx >= 0) {
            CompensationRate rate = allowanceRates.get(rateIdx);
            if (rate.percentage().compareTo(BigDecimal.ZERO) > 0) {
                entries.add(new OvertimeEntry(
                        incidentId, null, hoursDecimal, rate.percentage(), subDate, fromTime, toTime, true));
            }
        }
    }

    private static List<int[]> splitAtMidnight(int fromMin, int toMin) {
        List<int[]> result = new ArrayList<>();
        int current = fromMin;
        while (current < toMin) {
            int nextMidnight = ((current / (24 * 60)) + 1) * (24 * 60);
            int end = Math.min(nextMidnight, toMin);
            result.add(new int[] {current, end});
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
}
