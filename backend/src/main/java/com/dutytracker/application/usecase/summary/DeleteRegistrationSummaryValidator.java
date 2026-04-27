package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteRegistrationSummaryValidator implements RequestValidator<DeleteRegistrationSummaryRequest> {

    @Override
    public void validate(DeleteRegistrationSummaryRequest request) {
        // no-op
    }
}
