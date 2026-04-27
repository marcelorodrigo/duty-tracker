package com.dutytracker.usecase.validator.summary;

import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class AddOnCallDayEntryValidator implements RequestValidator<AddOnCallDayEntryRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;

    public AddOnCallDayEntryValidator(OnCallPeriodGateway onCallPeriodGateway) {
        this.onCallPeriodGateway = onCallPeriodGateway;
    }

    @Override
    public void validate(AddOnCallDayEntryRequest request) {
        onCallPeriodGateway
                .findById(request.onCallPeriodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
    }
}
