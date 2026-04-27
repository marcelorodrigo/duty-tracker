package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.domain.gateway.HolidayOverrideGateway;
import com.dutytracker.domain.gateway.OnCallPeriodGateway;
import com.dutytracker.domain.model.HolidayOverride;
import com.dutytracker.domain.model.OnCallPeriod;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListOnCallPeriodsUseCase implements UseCase<ListOnCallPeriodsRequest, OnCallPeriodListResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final ListOnCallPeriodsValidator validator;

    public ListOnCallPeriodsUseCase(OnCallPeriodGateway onCallPeriodGateway,
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
                period.id(), period.startDateTime(), period.endDateTime(),
                overrides.stream().map(HolidayOverride::date).toList(),
                period.createdAt()
        );
    }
}
