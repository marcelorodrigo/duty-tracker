package com.dutytracker.application.usecase.preferences;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetUserPreferencesValidator implements RequestValidator<GetUserPreferencesRequest> {

    @Override
    public void validate(GetUserPreferencesRequest request) {
        // No validation needed for empty request
    }
}
