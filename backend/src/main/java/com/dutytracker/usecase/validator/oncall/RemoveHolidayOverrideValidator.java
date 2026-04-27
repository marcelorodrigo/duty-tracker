package com.dutytracker.usecase.validator.oncall;

import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveHolidayOverrideValidator implements RequestValidator<RemoveHolidayOverrideRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;

    @Override
    public void validate(RemoveHolidayOverrideRequest request) {
        onCallPeriodGateway
                .findById(request.periodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
    }
}
