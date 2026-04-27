package com.dutytracker.usecase.validator.onboarding;

import com.dutytracker.usecase.request.onboarding.*;
import com.dutytracker.usecase.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetOnboardingStatusValidator implements RequestValidator<GetOnboardingStatusRequest> {

    @Override
    public void validate(GetOnboardingStatusRequest request) {
        // No validation needed for empty request
    }
}
