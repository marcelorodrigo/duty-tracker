package com.dutytracker.usecase.oncall;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.gateway.profile.EngineerProfileGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.validator.oncall.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateOnCallDayEntriesUseCase
        implements UseCase<CalculateOnCallDayEntriesRequest, OnCallDayEntriesResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
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

        List<HolidayOverride> overrides = holidayOverrideGateway.findByOnCallPeriodId(periodId);
        Set<LocalDate> holidayOverrideDates =
                overrides.stream().map(HolidayOverride::date).collect(Collectors.toSet());

        LocalDate startDate = period.startDateTime().toLocalDate();
        LocalDate endDate = period.endDateTime().toLocalDate();

        List<OnCallDayEntry> entries = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            double rawHours = computeRawHours(period, startDate, endDate, current);
            StandbyRateType rateType = determineRateType(current, holidayOverrideDates);
            boolean isWorkingDay = profile.workingDays().contains(current.getDayOfWeek());
            boolean capped = false;
            if (isWorkingDay && rawHours > 15.0) {
                rawHours = 15.0;
                capped = true;
            }
            BigDecimal hours = BigDecimal.valueOf(rawHours).setScale(4, RoundingMode.HALF_UP);
            entries.add(new OnCallDayEntry(periodId, current, hours, rateType, capped));
            current = current.plusDays(1);
        }

        List<OnCallDayEntryResponse> responses = entries.stream()
                .map(e -> new OnCallDayEntryResponse(e.date(), e.hours(), e.rateType(), e.capped()))
                .toList();

        return new OnCallDayEntriesResponse(periodId, responses);
    }

    private double computeRawHours(OnCallPeriod period, LocalDate startDate, LocalDate endDate, LocalDate day) {
        boolean isStart = day.equals(startDate);
        boolean isEnd = day.equals(endDate);

        if (isStart && isEnd) {
            int startMinutes = period.startDateTime().getHour() * 60
                    + period.startDateTime().getMinute();
            int endMinutes =
                    period.endDateTime().getHour() * 60 + period.endDateTime().getMinute();
            return (endMinutes - startMinutes) / 60.0;
        } else if (isStart) {
            return 24.0
                    - period.startDateTime().getHour()
                    - period.startDateTime().getMinute() / 60.0;
        } else if (isEnd) {
            return period.endDateTime().getHour() + period.endDateTime().getMinute() / 60.0;
        } else {
            return 24.0;
        }
    }

    private StandbyRateType determineRateType(LocalDate day, Set<LocalDate> holidayOverrideDates) {
        if (day.getDayOfWeek() == DayOfWeek.SUNDAY || holidayOverrideDates.contains(day)) {
            return StandbyRateType.SUNDAY_HOLIDAY;
        }
        return StandbyRateType.WEEKDAY_SATURDAY;
    }
}
