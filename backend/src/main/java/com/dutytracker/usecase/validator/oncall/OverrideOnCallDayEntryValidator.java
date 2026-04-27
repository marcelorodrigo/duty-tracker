package com.dutytracker.usecase.validator.oncall;


import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.OnCallDayEntryGateway;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.validator.RequestValidator;
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
