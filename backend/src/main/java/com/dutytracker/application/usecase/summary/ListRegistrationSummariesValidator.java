package com.dutytracker.application.usecase.summary;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class ListRegistrationSummariesValidator implements RequestValidator<ListRegistrationSummariesRequest> {

    @Override
    public void validate(ListRegistrationSummariesRequest request) {
        // no-op
    }
}
