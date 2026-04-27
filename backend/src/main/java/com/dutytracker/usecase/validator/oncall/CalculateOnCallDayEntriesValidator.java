package com.dutytracker.usecase.validator.oncall;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
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
