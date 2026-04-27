package com.dutytracker.usecase.summary;

import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.response.summary.*;
import com.dutytracker.usecase.validator.summary.*;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.usecase.oncall.OnCallDayEntryResponse;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.domain.OnCallDayEntry;
import org.springframework.stereotype.Service;

@Service
public class AddOnCallDayEntryUseCase implements UseCase<AddOnCallDayEntryRequest, OnCallDayEntryResponse> {

    private final OnCallDayEntryGateway onCallDayEntryGateway;
    private final AddOnCallDayEntryValidator validator;

    public AddOnCallDayEntryUseCase(OnCallDayEntryGateway onCallDayEntryGateway,
                                     AddOnCallDayEntryValidator validator) {
        this.onCallDayEntryGateway = onCallDayEntryGateway;
        this.validator = validator;
    }

    @Override
    public OnCallDayEntryResponse execute(AddOnCallDayEntryRequest request) {
        validator.validate(request);

        OnCallDayEntry saved = onCallDayEntryGateway.save(
                new OnCallDayEntry(null, request.onCallPeriodId(), request.date(),
                        request.hours(), request.rateType(), false, false, true));

        return new OnCallDayEntryResponse(
                saved.id(), saved.date(), saved.hours(), saved.rateType(),
                saved.capped(), saved.timeForTimeFlag(), saved.manualOverride());
    }
}
