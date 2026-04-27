package com.dutytracker.usecase.validator.onboarding;

import com.dutytracker.usecase.validator.RequestValidator;
import com.dutytracker.usecase.request.onboarding.*;
import com.dutytracker.domain.exceptions.InvalidOnCallPeriodException;
import com.dutytracker.gateway.preferences.UserPreferencesGateway;
import com.dutytracker.domain.OnboardingStep;
import org.springframework.stereotype.Component;

@Component
public class AdvanceOnboardingStepValidator implements RequestValidator<AdvanceOnboardingStepRequest> {

    private final UserPreferencesGateway preferencesGateway;

    public AdvanceOnboardingStepValidator(UserPreferencesGateway preferencesGateway) {
        this.preferencesGateway = preferencesGateway;
    }

    @Override
    public void validate(AdvanceOnboardingStepRequest request) {
        OnboardingStep stored = preferencesGateway.find()
                .map(p -> p.onboardingStep())
                .orElse(OnboardingStep.PROFILE);
        if (request.currentStep() != stored) {
            throw new InvalidOnCallPeriodException(
                    "Current step mismatch. Expected " + stored + " but got " + request.currentStep());
        }
    }
}
