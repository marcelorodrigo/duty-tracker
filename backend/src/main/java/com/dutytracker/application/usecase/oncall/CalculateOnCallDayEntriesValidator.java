package com.dutytracker.application.usecase.oncall;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import org.springframework.stereotype.Component;

@Component
public class CalculateOnCallDayEntriesValidator implements RequestValidator<CalculateOnCallDayEntriesRequest> {

    @Override
    public void validate(CalculateOnCallDayEntriesRequest request) {
        if (request.periodId() == null) {
            throw new InvalidOnCallPeriodException("periodId must not be null");
        }
    }
}
