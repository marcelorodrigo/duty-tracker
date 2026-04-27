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
public class ListOnCallPeriodsUseCase implements UseCase<ListOnCallPeriodsRequest, OnCallPeriodListResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final ListOnCallPeriodsValidator validator;

    public ListOnCallPeriodsUseCase(
            OnCallPeriodGateway onCallPeriodGateway,
            HolidayOverrideGateway holidayOverrideGateway,
            ListOnCallPeriodsValidator validator) {
        this.onCallPeriodGateway = onCallPeriodGateway;
        this.holidayOverrideGateway = holidayOverrideGateway;
        this.validator = validator;
    }

    @Override
    public OnCallPeriodListResponse execute(ListOnCallPeriodsRequest request) {
        validator.validate(request);
        List<OnCallPeriodResponse> responses = onCallPeriodGateway.findAll().stream()
                .map(period -> toResponse(period, holidayOverrideGateway.findByOnCallPeriodId(period.id())))
                .toList();
        return new OnCallPeriodListResponse(responses);
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
