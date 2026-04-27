package com.dutytracker.application.usecase.profile;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetEngineerProfileValidator implements RequestValidator<GetEngineerProfileRequest> {

    @Override
    public void validate(GetEngineerProfileRequest request) {
        // No validation needed for empty request
    }
}
