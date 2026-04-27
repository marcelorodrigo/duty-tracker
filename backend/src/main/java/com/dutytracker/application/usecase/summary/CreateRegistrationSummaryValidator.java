package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.OnCallPeriodGateway;
import org.springframework.stereotype.Component;

@Component
public class CreateRegistrationSummaryValidator implements RequestValidator<CreateRegistrationSummaryRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;

    public CreateRegistrationSummaryValidator(OnCallPeriodGateway onCallPeriodGateway) {
        this.onCallPeriodGateway = onCallPeriodGateway;
    }

    @Override
    public void validate(CreateRegistrationSummaryRequest request) {
        onCallPeriodGateway.findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
    }
}
