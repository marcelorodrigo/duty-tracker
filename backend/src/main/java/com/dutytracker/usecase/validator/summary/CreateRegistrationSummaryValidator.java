package com.dutytracker.usecase.validator.summary;

import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateRegistrationSummaryValidator implements RequestValidator<CreateRegistrationSummaryRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;

    @Override
    public void validate(CreateRegistrationSummaryRequest request) {
        onCallPeriodGateway
                .findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
    }
}
