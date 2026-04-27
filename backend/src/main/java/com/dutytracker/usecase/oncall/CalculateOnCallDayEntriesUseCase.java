package com.dutytracker.usecase.oncall;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.EngineerProfileGateway;
import com.dutytracker.gateway.HolidayOverrideGateway;
import com.dutytracker.gateway.OnCallDayEntryGateway;
import com.dutytracker.gateway.OnCallPeriodGateway;
import com.dutytracker.gateway.PublicHolidayGateway;
import com.dutytracker.domain.model.EngineerProfile;
import com.dutytracker.domain.model.HolidayOverride;
import com.dutytracker.domain.model.OnCallDayEntry;
import com.dutytracker.domain.model.OnCallPeriod;
import com.dutytracker.domain.model.StandbyRateType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CalculateOnCallDayEntriesUseCase implements UseCase<CalculateOnCallDayEntriesRequest, OnCallDayEntriesResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final EngineerProfileGateway engineerProfileGateway;
    private final OnCallDayEntryGateway onCallDayEntryGateway;
    private final PublicHolidayGateway publicHolidayGateway;
    private final CalculateOnCallDayEntriesValidator validator;

    public CalculateOnCallDayEntriesUseCase(OnCallPeriodGateway onCallPeriodGateway,
                                             HolidayOverrideGateway holidayOverrideGateway,
                                             EngineerProfileGateway engineerProfileGateway,
                                             OnCallDayEntryGateway onCallDayEntryGateway,
                                             PublicHolidayGateway publicHolidayGateway,
                                             CalculateOnCallDayEntriesValidator validator) {
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.holidayOverrideGateway = holidayOverrideGateway;
        this.engineerProfileGateway = engineerProfileGateway;
        this.onCallDayEntryGateway = onCallDayEntryGateway;
        this.publicHolidayGateway = publicHolidayGateway;
        this.validator = validator;
    }

    @Override
    public OnCallDayEntriesResponse execute(CalculateOnCallDayEntriesRequest request) {
        validator.validate(request);

        Long periodId = request.periodId();

        OnCallPeriod period = onCallPeriodGateway.findById(periodId)
                .orElseThrow(() -> new InvalidOnCallPeriodException("OnCallPeriod not found: " + periodId));

        EngineerProfile profile = engineerProfileGateway.find()
                .orElseThrow(() -> new InvalidOnCallPeriodException("EngineerProfile not found"));

        List<HolidayOverride> overrides = holidayOverrideGateway.findByOnCallPeriodId(periodId);
        Set<LocalDate> holidayOverrideDates = overrides.stream()
                .map(HolidayOverride::date)
                .collect(Collectors.toSet());

        LocalDate startDate = period.startDateTime().toLocalDate();
        LocalDate endDate = period.endDateTime().toLocalDate();

        List<OnCallDayEntry> newEntries = new ArrayList<>();
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
            newEntries.add(new OnCallDayEntry(null, periodId, current, hours, rateType, capped, false, false));
            current = current.plusDays(1);
        }

        // Delete existing entries before saving recalculated ones
        onCallDayEntryGateway.findByOnCallPeriodId(periodId)
                .forEach(e -> onCallDayEntryGateway.deleteById(e.id()));

        List<OnCallDayEntry> saved = onCallDayEntryGateway.saveAll(newEntries);

        List<OnCallDayEntryResponse> responses = saved.stream()
                .map(e -> new OnCallDayEntryResponse(
                        e.id(), e.date(), e.hours(), e.rateType(),
                        e.capped(), e.timeForTimeFlag(), e.manualOverride()))
                .toList();

        return new OnCallDayEntriesResponse(periodId, responses);
    }

    private double computeRawHours(OnCallPeriod period, LocalDate startDate, LocalDate endDate, LocalDate day) {
        boolean isStart = day.equals(startDate);
        boolean isEnd = day.equals(endDate);

        if (isStart && isEnd) {
            int startMinutes = period.startDateTime().getHour() * 60 + period.startDateTime().getMinute();
            int endMinutes = period.endDateTime().getHour() * 60 + period.endDateTime().getMinute();
            return (endMinutes - startMinutes) / 60.0;
        } else if (isStart) {
            return 24.0 - period.startDateTime().getHour() - period.startDateTime().getMinute() / 60.0;
        } else if (isEnd) {
            return period.endDateTime().getHour() + period.endDateTime().getMinute() / 60.0;
        } else {
            return 24.0;
        }
    }

    private StandbyRateType determineRateType(LocalDate day, Set<LocalDate> holidayOverrideDates) {
        if (day.getDayOfWeek() == DayOfWeek.SUNDAY
                || publicHolidayGateway.isHoliday(day)
                || holidayOverrideDates.contains(day)) {
            return StandbyRateType.SUNDAY_HOLIDAY;
        }
        return StandbyRateType.WEEKDAY_SATURDAY;
    }
}
