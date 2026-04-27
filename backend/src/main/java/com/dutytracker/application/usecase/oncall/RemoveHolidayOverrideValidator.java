package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.OnCallPeriodGateway;
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
