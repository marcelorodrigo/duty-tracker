package com.dutytracker.usecase.validator.preferences;


import com.dutytracker.usecase.request.preferences.*;
import com.dutytracker.usecase.validator.RequestValidator;
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
