package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.usecase.request.summary.*;
import org.springframework.stereotype.Component;

@Component
public class DeleteRegistrationSummaryValidator implements RequestValidator<DeleteRegistrationSummaryRequest> {

    @Override
    public void validate(DeleteRegistrationSummaryRequest request) {
        // no-op
    }
}
