package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Hours;
import com.github.marcelorodrigo.dutytracker.domain.OnCallDayEntry;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.StandbyRateType;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntriesResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallDayEntryResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class OnCallDayEntriesCalculator {

    private static final int MINUTES_PER_HOUR = 60;
    private static final int MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR;
    private static final int FULL_WORKING_DAY_CAP_MINUTES = 15 * MINUTES_PER_HOUR;

    public OnCallDayEntriesResponse calculate(
            OnCallPeriod period, EngineerProfile profile, Set<LocalDate> holidayDates) {
        Long periodId = period.id();

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
                .map(entry -> new OnCallDayEntryResponse(
                        entry.date(),
                        computeDayLabel(entry.date(), holidayDates),
                        entry.hours().value(),
                        entry.rateType(),
                        entry.capped()))
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

    private int toMinutes(LocalTime time) {
        return time.getHour() * MINUTES_PER_HOUR + time.getMinute();
    }

    private StandbyRateType determineRateType(LocalDate day, Set<LocalDate> holidayDates) {
        if (DayOfWeek.SUNDAY.equals(day.getDayOfWeek()) || holidayDates.contains(day)) {
            return StandbyRateType.SUNDAY_HOLIDAY;
        }
        return StandbyRateType.WEEKDAY_SATURDAY;
    }
}
