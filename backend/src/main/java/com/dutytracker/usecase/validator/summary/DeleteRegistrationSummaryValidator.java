package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteRegistrationSummaryValidator implements RequestValidator<DeleteRegistrationSummaryRequest> {

    @Override
    public void validate(DeleteRegistrationSummaryRequest request) {
        // no-op
    }
}
