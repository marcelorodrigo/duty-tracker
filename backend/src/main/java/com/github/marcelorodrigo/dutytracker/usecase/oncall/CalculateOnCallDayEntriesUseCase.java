package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.EngineerProfile;
import com.github.marcelorodrigo.dutytracker.domain.Holiday;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
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

@Service
@RequiredArgsConstructor
public class CalculateOnCallDayEntriesUseCase
        implements UseCase<CalculateOnCallDayEntriesRequest, OnCallDayEntriesResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayGateway holidayGateway;
    private final EngineerProfileGateway engineerProfileGateway;
    private final CalculateOnCallDayEntriesValidator validator;

    @Override
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
            double rawHours = computeRawHours(period, startDate, endDate, current, isWorkingDay, profile);
            StandbyRateType rateType = determineRateType(current, holidayDates);
            boolean capped = false;
            boolean isPartialDay = current.equals(startDate) || current.equals(endDate);
            if (isWorkingDay && !isPartialDay && rawHours > 15.0) {
                rawHours = 15.0;
                capped = true;
            }
            BigDecimal hours = BigDecimal.valueOf(rawHours).setScale(4, RoundingMode.HALF_UP);
            entries.add(new OnCallDayEntry(periodId, current, hours, rateType, capped));
            current = current.plusDays(1);
        }

        List<OnCallDayEntryResponse> responses = entries.stream()
                .map(e -> new OnCallDayEntryResponse(
                        e.date(), computeDayLabel(e.date(), holidayDates), e.hours(), e.rateType(), e.capped()))
                .toList();

        return new OnCallDayEntriesResponse(periodId, responses);
    }

    private String computeDayLabel(LocalDate date, Set<LocalDate> holidayDates) {
        if (holidayDates.contains(date)) {
            return "Holiday";
        }
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private double computeRawHours(
            OnCallPeriod period,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate day,
            boolean isWorkingDay,
            EngineerProfile profile) {
        boolean isStart = day.equals(startDate);
        boolean isEnd = day.equals(endDate);

        if (isStart && isEnd) {
            int startMinutes = period.startDateTime().getHour() * 60
                    + period.startDateTime().getMinute();
            int endMinutes =
                    period.endDateTime().getHour() * 60 + period.endDateTime().getMinute();
            return (endMinutes - startMinutes) / 60.0;
        } else if (isStart) {
            if (!isWorkingDay) {
                return 24.0
                        - period.startDateTime().getHour()
                        - period.startDateTime().getMinute() / 60.0;
            }
            double workStart = toHours(profile.workStartTime());
            double workEnd = toHours(profile.workEndTime());
            double onCallStart =
                    period.startDateTime().getHour() + period.startDateTime().getMinute() / 60.0;
            double preWork = Math.max(0.0, workStart - onCallStart);
            double postWork = Math.max(0.0, 24.0 - Math.max(onCallStart, workEnd));
            return preWork + postWork;
        } else if (isEnd) {
            if (!isWorkingDay) {
                return period.endDateTime().getHour() + period.endDateTime().getMinute() / 60.0;
            }
            double workStart = toHours(profile.workStartTime());
            double workEnd = toHours(profile.workEndTime());
            double onCallEnd =
                    period.endDateTime().getHour() + period.endDateTime().getMinute() / 60.0;
            double preWork = Math.min(onCallEnd, workStart);
            double postWork = Math.max(0.0, onCallEnd - workEnd);
            return preWork + postWork;
        } else {
            return 24.0;
        }
    }

    private double toHours(java.time.LocalTime time) {
        return time.getHour() + time.getMinute() / 60.0;
    }

    private StandbyRateType determineRateType(LocalDate day, Set<LocalDate> holidayDates) {
        if (day.getDayOfWeek() == DayOfWeek.SUNDAY || holidayDates.contains(day)) {
            return StandbyRateType.SUNDAY_HOLIDAY;
        }
        return StandbyRateType.WEEKDAY_SATURDAY;
    }
}
