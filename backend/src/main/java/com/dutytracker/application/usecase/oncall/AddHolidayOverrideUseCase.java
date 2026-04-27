package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.HolidayOverrideGateway;
import com.dutytracker.domain.gateway.OnCallPeriodGateway;
import com.dutytracker.domain.model.HolidayOverride;
import com.dutytracker.domain.model.OnCallPeriod;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddHolidayOverrideUseCase implements UseCase<AddHolidayOverrideRequest, OnCallPeriodResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final AddHolidayOverrideValidator validator;

    public AddHolidayOverrideUseCase(OnCallPeriodGateway onCallPeriodGateway,
                                     HolidayOverrideGateway holidayOverrideGateway,
                                     AddHolidayOverrideValidator validator) {
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.holidayOverrideGateway = holidayOverrideGateway;
        this.validator = validator;
    }

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
                period.id(), period.startDateTime(), period.endDateTime(),
                overrides.stream().map(HolidayOverride::date).toList(),
                period.createdAt()
        );
    }
}
