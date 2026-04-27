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
