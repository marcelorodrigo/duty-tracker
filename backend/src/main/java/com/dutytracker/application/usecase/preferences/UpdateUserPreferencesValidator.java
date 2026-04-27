package com.dutytracker.application.usecase.preferences;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserPreferencesValidator implements RequestValidator<UpdateUserPreferencesRequest> {

    @Override
    public void validate(UpdateUserPreferencesRequest request) {
        if (request.colorScheme() == null) {
            throw new IllegalArgumentException("colorScheme must not be null");
        }
    }
}
