package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteRegistrationSummaryValidator implements RequestValidator<DeleteRegistrationSummaryRequest> {

    @Override
    public void validate(DeleteRegistrationSummaryRequest request) {
        // no-op
    }
}
