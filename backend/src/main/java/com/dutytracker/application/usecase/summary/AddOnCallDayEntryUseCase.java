package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.UseCase;
import com.dutytracker.application.usecase.oncall.OnCallDayEntryResponse;
import com.dutytracker.domain.gateway.OnCallDayEntryGateway;
import com.dutytracker.domain.model.OnCallDayEntry;
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
