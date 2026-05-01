package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.HolidayOverride;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.*;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.HolidayNotFoundException;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.RemoveHolidayOverrideRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.RemoveHolidayOverrideValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoveHolidayOverrideUseCase implements UseCase<RemoveHolidayOverrideRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final RemoveHolidayOverrideValidator validator;

    @Override
    public OnCallPeriodResponse execute(RemoveHolidayOverrideRequest request) {
        validator.validate(request);
        Long holidayOverrideId = holidayOverrideGateway.findByOnCallPeriodId(request.periodId()).stream()
                .filter(o -> o.date().equals(request.date()))
                .findFirst()
                .map(HolidayOverride::id)
                .orElseThrow(() -> new HolidayNotFoundException("Holiday not found for date: " + request.date()));
        holidayOverrideGateway.deleteById(holidayOverrideId);
        OnCallPeriod period = onCallPeriodGateway.findById(request.periodId()).orElseThrow();
        List<HolidayOverride> remaining = holidayOverrideGateway.findByOnCallPeriodId(request.periodId());
        return toResponse(period, remaining);
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
