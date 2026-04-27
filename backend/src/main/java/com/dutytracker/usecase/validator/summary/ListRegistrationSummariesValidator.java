package com.dutytracker.usecase.validator.summary;

import com.dutytracker.usecase.request.summary.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListRegistrationSummariesValidator implements RequestValidator<ListRegistrationSummariesRequest> {

    @Override
    public void validate(ListRegistrationSummariesRequest request) {
        // no-op
    }
}
