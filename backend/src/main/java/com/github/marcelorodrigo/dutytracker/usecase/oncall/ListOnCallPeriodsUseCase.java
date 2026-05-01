package com.github.marcelorodrigo.dutytracker.usecase.oncall;

import com.github.marcelorodrigo.dutytracker.domain.*;
import com.github.marcelorodrigo.dutytracker.domain.HolidayOverride;
import com.github.marcelorodrigo.dutytracker.domain.OnCallPeriod;
import com.github.marcelorodrigo.dutytracker.domain.exceptions.*;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.HolidayOverrideGateway;
import com.github.marcelorodrigo.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.github.marcelorodrigo.dutytracker.usecase.UseCase;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.ListOnCallPeriodsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodListResponse;
import com.github.marcelorodrigo.dutytracker.usecase.response.oncall.OnCallPeriodResponse;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.*;
import com.github.marcelorodrigo.dutytracker.usecase.validator.oncall.ListOnCallPeriodsValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListOnCallPeriodsUseCase implements UseCase<ListOnCallPeriodsRequest, OnCallPeriodListResponse> {

    private final OnCallPeriodGateway onCallPeriodGateway;
    private final HolidayOverrideGateway holidayOverrideGateway;
    private final ListOnCallPeriodsValidator validator;

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
