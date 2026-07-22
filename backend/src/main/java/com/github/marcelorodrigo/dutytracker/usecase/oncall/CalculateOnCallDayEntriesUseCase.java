package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Holiday;
import com.github.marcelorodrigo.dutytracker.domain.Hours;
import com.github.marcelorodrigo.dutytracker.domain.OnCallDayEntry;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.StandbyRateType;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.ProfileNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.gateway.profile.EngineerProfileGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateOnCallDayEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntryResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CalculateOnCallDayEntriesValidator;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalculateOnCallDayEntriesUseCase
        implements UseCase<CalculateOnCallDayEntriesRequest, OnCallDayEntriesResponse> {

    private static final int MINUTES_PER_HOUR = 60;
    private static final int MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR;
    private static final int FULL_WORKING_DAY_CAP_MINUTES = 15 * MINUTES_PER_HOUR;

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;
    private final EngineerProfileGateway engineerProfileGateway;
    private final CalculateOnCallDayEntriesValidator validator;

    @Override
    @Transactional(readOnly = true)
    public OnCallDayEntriesResponse execute(CalculateOnCallDayEntriesRequest request) {
        validator.validate(request);

        Long periodId = request.periodId();

        OnCallPeriod period = onCallPeriodGateway
                .findById(periodId)
                .orElseThrow(() -> new InvalidOnCallPeriodException("OnCallPeriod not found: " + periodId));

        EngineerProfile profile = engineerProfileGateway
                .find()
                .orElseThrow(() -> new ProfileNotFoundException("EngineerProfile not found"));

        Set<LocalDate> holidayDates = holidayGateway.findByOnCallPeriodId(periodId).stream()
                .map(Holiday::date)
                .collect(Collectors.toSet());

        LocalDate startDate = period.startDateTime().toLocalDate();
        LocalDate endDate = period.endDateTime().toLocalDate();

        List<OnCallDayEntry> entries = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            boolean isWorkingDay =
                    profile.workingDays().contains(current.getDayOfWeek()) && !holidayDates.contains(current);
            int rawMinutes = computeRawMinutes(period, startDate, endDate, current, isWorkingDay, profile);
            StandbyRateType rateType = determineRateType(current, holidayDates);
            boolean capped = false;
            boolean isPartialDay = current.equals(startDate) || current.equals(endDate);
            if (isWorkingDay && !isPartialDay && rawMinutes > FULL_WORKING_DAY_CAP_MINUTES) {
                rawMinutes = FULL_WORKING_DAY_CAP_MINUTES;
                capped = true;
            }
            entries.add(new OnCallDayEntry(periodId, current, Hours.fromMinutes(rawMinutes), rateType, capped));
            current = current.plusDays(1);
        }

        List<OnCallDayEntryResponse> responses = entries.stream()
                .map(e -> new OnCallDayEntryResponse(
                        e.date(),
                        computeDayLabel(e.date(), holidayDates),
                        e.hours().value(),
                        e.rateType(),
                        e.capped()))
                .toList();

        return new OnCallDayEntriesResponse(periodId, responses);
    }

    private String computeDayLabel(LocalDate date, Set<LocalDate> holidayDates) {
        if (holidayDates.contains(date)) {
            return "Holiday";
        }
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private int computeRawMinutes(
            OnCallPeriod period,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate day,
            boolean isWorkingDay,
            EngineerProfile profile) {
        boolean isStart = day.equals(startDate);
        boolean isEnd = day.equals(endDate);

        if (isStart && isEnd) {
            int startMinutes = toMinutes(period.startDateTime().toLocalTime());
            int endMinutes = toMinutes(period.endDateTime().toLocalTime());
            return endMinutes - startMinutes;
        } else if (isStart) {
            int onCallStart = toMinutes(period.startDateTime().toLocalTime());
            if (!isWorkingDay) {
                return MINUTES_PER_DAY - onCallStart;
            }
            int workStart = toMinutes(profile.workStartTime());
            int workEnd = toMinutes(profile.workEndTime());
            int preWork = Math.max(0, workStart - onCallStart);
            int postWork = Math.max(0, MINUTES_PER_DAY - Math.max(onCallStart, workEnd));
            return preWork + postWork;
        } else if (isEnd) {
            int onCallEnd = toMinutes(period.endDateTime().toLocalTime());
            if (!isWorkingDay) {
                return onCallEnd;
            }
            int workStart = toMinutes(profile.workStartTime());
            int workEnd = toMinutes(profile.workEndTime());
            int preWork = Math.min(onCallEnd, workStart);
            int postWork = Math.max(0, onCallEnd - workEnd);
            return preWork + postWork;
        } else {
            return MINUTES_PER_DAY;
        }
    }

    private int toMinutes(java.time.LocalTime time) {
        return time.getHour() * MINUTES_PER_HOUR + time.getMinute();
    }

    private StandbyRateType determineRateType(LocalDate day, Set<LocalDate> holidayDates) {
        if (day.getDayOfWeek() == DayOfWeek.SUNDAY || holidayDates.contains(day)) {
            return StandbyRateType.SUNDAY_HOLIDAY;
        }
        return StandbyRateType.WEEKDAY_SATURDAY;
    }
}
