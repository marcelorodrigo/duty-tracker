package com.dutytracker.usecase.oncall;

import com.dutytracker.domain.*;
import com.dutytracker.gateway.holiday.PublicHolidayGateway;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.validator.oncall.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOnCallPeriodUseCase implements UseCase<CreateOnCallPeriodRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final PublicHolidayGateway publicHolidayGateway;
    private final CreateOnCallPeriodValidator validator;

    @Override
    @Transactional
    public OnCallPeriodResponse execute(CreateOnCallPeriodRequest request) {
        validator.validate(request);
        OnCallPeriod period = new OnCallPeriod(null, request.startDateTime(), request.endDateTime(), Instant.now());
        OnCallPeriod saved = onCallPeriodGateway.save(period);

        List<HolidayOverride> seededHolidays = seedHolidays(saved);

        return toResponse(saved, seededHolidays);
    }

    private List<HolidayOverride> seedHolidays(OnCallPeriod period) {
        LocalDate startDate = period.startDateTime().toLocalDate();
        LocalDate endDate = period.endDateTime().toLocalDate();

        List<LocalDate> holidayDates = new ArrayList<>();
        for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
            publicHolidayGateway.getHolidays(year).stream()
                    .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                    .forEach(holidayDates::add);
        }

        List<HolidayOverride> saved = new ArrayList<>();
        for (LocalDate date : holidayDates) {
            saved.add(holidayOverrideGateway.save(new HolidayOverride(null, period.id(), date)));
        }
        return saved;
    }

    private OnCallPeriodResponse toResponse(OnCallPeriod period, List<HolidayOverride> overrides) {
        return new OnCallPeriodResponse(
                period.id(),
                period.startDateTime(),
                period.endDateTime(),
                overrides.stream().map(HolidayOverride::date).toList(),
                period.createdAt());
    }
}
