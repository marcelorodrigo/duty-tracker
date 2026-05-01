package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.HolidayOverride;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.gateway.holiday.PublicHolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CreateOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.CreateOnCallPeriodValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        OnCallPeriod period =
                new OnCallPeriod(null, request.startDateTime(), request.endDateTime(), LocalDateTime.now());
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
