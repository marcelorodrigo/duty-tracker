package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetRegistrationSummaryValidator implements RequestValidator<GetRegistrationSummaryRequest> {

    @Override
    public void validate(GetRegistrationSummaryRequest request) {
        // no-op
    }
}
