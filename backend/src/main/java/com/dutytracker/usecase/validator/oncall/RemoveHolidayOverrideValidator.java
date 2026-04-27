package com.dutytracker.usecase.validator.oncall;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import org.springframework.stereotype.Component;

@Component
public class RemoveHolidayOverrideValidator implements RequestValidator<RemoveHolidayOverrideRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;

    public RemoveHolidayOverrideValidator(OnCallPeriodGateway onCallPeriodGateway) {
        this.onCallPeriodGateway = onCallPeriodGateway;
    }

    @Override
    public void validate(RemoveHolidayOverrideRequest request) {
        onCallPeriodGateway.findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
    }
}
