package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetOnCallPeriodValidator implements RequestValidator<GetOnCallPeriodRequest> {

    @Override
    public void validate(GetOnCallPeriodRequest request) {
        // no-op: period existence is checked in the use case
    }
}
