package com.dutytracker.application.usecase.onboarding;

import com.dutytracker.domain.model.OnboardingStep;

public record AdvanceOnboardingStepRequest(OnboardingStep currentStep) {}
