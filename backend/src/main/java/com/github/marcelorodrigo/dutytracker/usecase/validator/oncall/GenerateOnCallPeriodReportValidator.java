package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GenerateOnCallPeriodReportRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GenerateOnCallPeriodReportValidator implements RequestValidator<GenerateOnCallPeriodReportRequest> {

    @Override
    public void validate(GenerateOnCallPeriodReportRequest request) {
        if (request == null) {
            throw new InvalidOnCallPeriodException("request must not be null");
        }
        if (request.periodId() == null || request.periodId() <= 0) {
            throw new InvalidOnCallPeriodException("periodId must be a positive number");
        }
    }
}
