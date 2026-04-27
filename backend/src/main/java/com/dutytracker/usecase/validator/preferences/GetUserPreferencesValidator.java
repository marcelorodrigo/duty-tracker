package com.dutytracker.usecase.validator.preferences;

import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetUserPreferencesValidator implements RequestValidator<GetUserPreferencesRequest> {

    @Override
    public void validate(GetUserPreferencesRequest request) {
        // No validation needed for empty request
    }
}
