package com.dutytracker.application.usecase.onboarding;

import com.dutytracker.application.usecase.RequestValidator;
import com.dutytracker.domain.exception.InvalidOnCallPeriodException;
import com.dutytracker.domain.gateway.UserPreferencesGateway;
import com.dutytracker.domain.model.OnboardingStep;
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
