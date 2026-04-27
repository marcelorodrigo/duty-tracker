package com.dutytracker.usecase.validator.summary;


import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
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
