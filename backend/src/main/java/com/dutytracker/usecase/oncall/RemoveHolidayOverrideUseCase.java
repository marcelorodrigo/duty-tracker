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
public class RemoveHolidayOverrideUseCase implements UseCase<RemoveHolidayOverrideRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final RemoveHolidayOverrideValidator validator;

    @Override
    public OnCallPeriodResponse execute(RemoveHolidayOverrideRequest request) {
        validator.validate(request);
        holidayOverrideGateway.findByOnCallPeriodId(request.periodId()).stream()
                .filter(o -> o.date().equals(request.date()))
                .findFirst()
                .ifPresent(o -> holidayOverrideGateway.deleteById(o.id()));
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
