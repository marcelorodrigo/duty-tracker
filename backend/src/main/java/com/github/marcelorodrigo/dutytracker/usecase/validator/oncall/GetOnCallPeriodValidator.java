package com.github.marcelorodrigo.dutytracker.usecase.validator.oncall;

import com.github.marcelorodrigo.dutytracker.usecase.request.oncall.GetOnCallPeriodRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetOnCallPeriodValidator implements RequestValidator<GetOnCallPeriodRequest> {

    @Override
    public void validate(GetOnCallPeriodRequest request) {
        // no-op: period existence is checked in the use case
    }
}
