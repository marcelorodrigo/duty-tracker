package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.OnCallPeriodGateway;
import org.springframework.stereotype.Component;

@Component
public class AddOnCallDayEntryValidator implements RequestValidator<AddOnCallDayEntryRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;

    public AddOnCallDayEntryValidator(OnCallPeriodGateway onCallPeriodGateway) {
        this.onCallPeriodGateway = onCallPeriodGateway;
    }

    @Override
    public void validate(AddOnCallDayEntryRequest request) {
        onCallPeriodGateway.findById(request.onCallPeriodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
    }
}
