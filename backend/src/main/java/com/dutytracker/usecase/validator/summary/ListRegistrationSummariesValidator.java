package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.usecase.request.summary.*;
import org.springframework.stereotype.Component;

@Component
public class ListRegistrationSummariesValidator implements RequestValidator<ListRegistrationSummariesRequest> {

    @Override
    public void validate(ListRegistrationSummariesRequest request) {
        // no-op
    }
}
