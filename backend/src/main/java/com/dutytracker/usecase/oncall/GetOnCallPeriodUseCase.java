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
import org.springframework.stereotype.Service;

@Service
public class GetOnCallPeriodUseCase implements UseCase<GetOnCallPeriodRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final GetOnCallPeriodValidator validator;

    public GetOnCallPeriodUseCase(
            OnCallPeriodGateway onCallPeriodGateway,
            HolidayOverrideGateway holidayOverrideGateway,
            GetOnCallPeriodValidator validator) {
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.holidayOverrideGateway = holidayOverrideGateway;
        this.validator = validator;
    }

    @Override
    public OnCallPeriodResponse execute(GetOnCallPeriodRequest request) {
        validator.validate(request);
        OnCallPeriod period = onCallPeriodGateway
                .findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
        List<HolidayOverride> overrides = holidayOverrideGateway.findByOnCallPeriodId(period.id());
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
