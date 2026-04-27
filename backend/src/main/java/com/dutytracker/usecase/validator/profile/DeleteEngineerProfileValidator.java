package com.dutytracker.usecase.validator.profile;

import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteEngineerProfileValidator implements RequestValidator<DeleteEngineerProfileRequest> {

    @Override
    public void validate(DeleteEngineerProfileRequest request) {
        // No validation needed for empty request
    }
}
