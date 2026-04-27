package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetRegistrationSummaryValidator implements RequestValidator<GetRegistrationSummaryRequest> {

    @Override
    public void validate(GetRegistrationSummaryRequest request) {
        // no-op
    }
}
