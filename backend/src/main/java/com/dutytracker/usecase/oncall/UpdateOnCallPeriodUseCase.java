package com.dutytracker.usecase.oncall;

import com.dutytracker.domain.*;
import com.dutytracker.domain.exceptions.*;
import com.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.response.oncall.*;
import com.dutytracker.usecase.validator.oncall.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateOnCallPeriodUseCase implements UseCase<UpdateOnCallPeriodRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final UpdateOnCallPeriodValidator validator;

    @Override
    public OnCallPeriodResponse execute(UpdateOnCallPeriodRequest request) {
        validator.validate(request);
        OnCallPeriod existing = onCallPeriodGateway
                .findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
        OnCallPeriod updated =
                new OnCallPeriod(existing.id(), request.startDateTime(), request.endDateTime(), existing.createdAt());
        OnCallPeriod saved = onCallPeriodGateway.save(updated);
        List<HolidayOverride> overrides = holidayOverrideGateway.findByOnCallPeriodId(saved.id());
        return toResponse(saved, overrides);
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
