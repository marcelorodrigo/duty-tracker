package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.usecase.request.summary.*;
import org.springframework.stereotype.Component;

@Component
public class GetRegistrationSummaryValidator implements RequestValidator<GetRegistrationSummaryRequest> {

    @Override
    public void validate(GetRegistrationSummaryRequest request) {
        // no-op
    }
}
