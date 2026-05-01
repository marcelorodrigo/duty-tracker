package com.github.marcelorodrigo.dutytracker.usecase.validator.profile;

import com.github.marcelorodrigo.dutytracker.usecase.request.profile.DeleteEngineerProfileRequest;
import com.github.marcelorodrigo.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class DeleteEngineerProfileValidator implements RequestValidator<DeleteEngineerProfileRequest> {

    @Override
    public void validate(DeleteEngineerProfileRequest request) {
        // No validation needed for empty request
    }
}
