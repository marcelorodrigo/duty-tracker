package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateOnCallDayEntriesRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalculateOnCallDayEntriesValidator implements RequestValidator<CalculateOnCallDayEntriesRequest> {

    @Override
    public void validate(CalculateOnCallDayEntriesRequest request) {
        if (request.periodId() == null) {
            throw new InvalidOnCallPeriodException("periodId must not be null");
        }
    }
}
