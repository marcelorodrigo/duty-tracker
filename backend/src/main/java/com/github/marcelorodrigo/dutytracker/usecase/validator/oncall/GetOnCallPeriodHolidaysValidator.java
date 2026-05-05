package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetOnCallPeriodHolidaysRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetOnCallPeriodHolidaysValidator implements RequestValidator<GetOnCallPeriodHolidaysRequest> {

    @Override
    public void validate(GetOnCallPeriodHolidaysRequest request) {
        // period existence is checked in the use case
    }
}
