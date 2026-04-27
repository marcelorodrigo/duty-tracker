package com.dutytracker.usecase.validator.oncall;


import com.dutytracker.usecase.request.oncall.*;
import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;
@Component
public class GetOnCallPeriodValidator implements RequestValidator<GetOnCallPeriodRequest> {

    @Override
    public void validate(GetOnCallPeriodRequest request) {
        // no-op: period existence is checked in the use case
    }
}
