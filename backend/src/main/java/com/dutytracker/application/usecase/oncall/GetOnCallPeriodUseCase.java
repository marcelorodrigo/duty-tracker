package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.HolidayOverrideGateway;
import com.dutytracker.domain.gateway.OnCallPeriodGateway;
import com.dutytracker.domain.model.HolidayOverride;
import com.dutytracker.domain.model.OnCallPeriod;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetOnCallPeriodUseCase implements UseCase<GetOnCallPeriodRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final GetOnCallPeriodValidator validator;

    public GetOnCallPeriodUseCase(OnCallPeriodGateway onCallPeriodGateway,
                                  HolidayOverrideGateway holidayOverrideGateway,
                                  GetOnCallPeriodValidator validator) {
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.holidayOverrideGateway = holidayOverrideGateway;
        this.validator = validator;
    }

    @Override
    public OnCallPeriodResponse execute(GetOnCallPeriodRequest request) {
        validator.validate(request);
        OnCallPeriod period = onCallPeriodGateway.findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
        List<HolidayOverride> overrides = holidayOverrideGateway.findByOnCallPeriodId(period.id());
        return toResponse(period, overrides);
    }

    private OnCallPeriodResponse toResponse(OnCallPeriod period, List<HolidayOverride> overrides) {
        return new OnCallPeriodResponse(
                period.id(), period.startDateTime(), period.endDateTime(),
                overrides.stream().map(HolidayOverride::date).toList(),
                period.createdAt()
        );
    }
}
