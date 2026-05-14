package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.CalculateEarningsRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalculateEarningsValidator implements RequestValidator<CalculateEarningsRequest> {

    @Override
    public void validate(CalculateEarningsRequest request) {
        if (request.periodId() == null) {
            throw new InvalidOnCallPeriodException("periodId must not be null");
        }
    }
}
