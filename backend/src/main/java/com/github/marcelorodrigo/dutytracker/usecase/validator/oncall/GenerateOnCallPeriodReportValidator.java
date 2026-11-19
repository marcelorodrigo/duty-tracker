package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GenerateOnCallPeriodReportRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerateOnCallPeriodReportValidator implements RequestValidator<GenerateOnCallPeriodReportRequest> {

    @Override
    public void validate(GenerateOnCallPeriodReportRequest request) {
        // no-op: period existence is checked in the use case
    }
}
