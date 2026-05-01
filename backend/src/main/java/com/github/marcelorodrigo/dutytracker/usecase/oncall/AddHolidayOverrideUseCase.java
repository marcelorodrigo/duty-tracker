package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.HolidayOverride;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.*;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.AddHolidayOverrideRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.AddHolidayOverrideValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddHolidayOverrideUseCase implements UseCase<AddHolidayOverrideRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final AddHolidayOverrideValidator validator;

    @Override
    public OnCallPeriodResponse execute(AddHolidayOverrideRequest request) {
        validator.validate(request);
        holidayOverrideGateway.save(new HolidayOverride(null, request.periodId(), request.date()));
        OnCallPeriod period = onCallPeriodGateway.findById(request.periodId()).orElseThrow();
        List<HolidayOverride> overrides = holidayOverrideGateway.findByOnCallPeriodId(request.periodId());
        return toResponse(period, overrides);
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
