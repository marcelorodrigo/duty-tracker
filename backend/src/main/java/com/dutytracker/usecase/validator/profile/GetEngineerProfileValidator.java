package com.dutytracker.usecase.validator.profile;


import com.dutytracker.usecase.request.profile.*;
import com.dutytracker.usecase.validator.RequestValidator;
import org.springframework.stereotype.Component;
@Component
public class GetEngineerProfileValidator implements RequestValidator<GetEngineerProfileRequest> {

    @Override
    public void validate(GetEngineerProfileRequest request) {
        // No validation needed for empty request
    }
}
