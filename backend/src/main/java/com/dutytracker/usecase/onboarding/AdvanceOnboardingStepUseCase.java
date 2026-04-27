package com.dutytracker.usecase.onboarding;

import com.dutytracker.usecase.request.onboarding.*;
import com.dutytracker.usecase.response.onboarding.*;
import com.dutytracker.usecase.validator.onboarding.*;
import com.dutytracker.domain.*;

import com.dutytracker.usecase.UseCase;
import com.dutytracker.gateway.preferences.UserPreferencesGateway;
import org.springframework.stereotype.Service;

@Service
public class AdvanceOnboardingStepUseCase implements UseCase<AdvanceOnboardingStepRequest, OnboardingStatusResponse> {

    private final UserPreferencesGateway preferencesGateway;
    private final AdvanceOnboardingStepValidator validator;

    public AdvanceOnboardingStepUseCase(UserPreferencesGateway preferencesGateway,
                                        AdvanceOnboardingStepValidator validator) {
        this.preferencesGateway = preferencesGateway;
        this.validator = validator;
    }

    @Override
    public OnboardingStatusResponse execute(AdvanceOnboardingStepRequest request) {
        validator.validate(request);
        OnboardingStep nextStep = nextStep(request.currentStep());
        UserPreferences existing = preferencesGateway.find()
                .orElse(new UserPreferences(null, ColorScheme.AUTO, OnboardingStep.PROFILE));
        UserPreferences updated = new UserPreferences(existing.id(), existing.colorScheme(), nextStep);
        preferencesGateway.save(updated);
        return new OnboardingStatusResponse(nextStep, nextStep == OnboardingStep.COMPLETE);
    }

    private OnboardingStep nextStep(OnboardingStep current) {
        return switch (current) {
            case PROFILE -> OnboardingStep.PREFERENCES;
            case PREFERENCES -> OnboardingStep.COMPENSATION_RATES;
            case COMPENSATION_RATES -> OnboardingStep.COMPLETE;
            case COMPLETE -> OnboardingStep.COMPLETE;
        };
    }
}
