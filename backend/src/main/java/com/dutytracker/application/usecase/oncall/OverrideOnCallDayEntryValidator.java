package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.OnCallDayEntryGateway;
import org.springframework.stereotype.Component;

@Component
public class OverrideOnCallDayEntryValidator implements RequestValidator<OverrideOnCallDayEntryRequest> {

    private final OnCallDayEntryGateway onCallDayEntryGateway;

    public OverrideOnCallDayEntryValidator(OnCallDayEntryGateway onCallDayEntryGateway) {
        this.onCallDayEntryGateway = onCallDayEntryGateway;
    }

    @Override
    public void validate(OverrideOnCallDayEntryRequest request) {
        onCallDayEntryGateway.findById(request.entryId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Day entry not found"));
    }
}
