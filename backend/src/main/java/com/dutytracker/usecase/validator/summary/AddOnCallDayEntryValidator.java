package com.dutytracker.usecase.validator.summary;

import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.oncall.OnCallPeriodGateway;
import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddOnCallDayEntryValidator implements RequestValidator<AddOnCallDayEntryRequest> {

    private final OnCallPeriodGateway onCallPeriodGateway;

    @Override
    public void validate(AddOnCallDayEntryRequest request) {
        onCallPeriodGateway
                .findById(request.onCallPeriodId())
                .orElseThrow(() -> new InvalidOnCallPeriodException("Period not found"));
    }
}
