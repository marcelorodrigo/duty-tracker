package com.dutytracker.application.usecase.onboarding;

import com.dutytracker.application.usecase.RequestValidator;
import org.springframework.stereotype.Component;

@Component
public class GetOnboardingStatusValidator implements RequestValidator<GetOnboardingStatusRequest> {

    @Override
    public void validate(GetOnboardingStatusRequest request) {
        // No validation needed for empty request
    }
}
