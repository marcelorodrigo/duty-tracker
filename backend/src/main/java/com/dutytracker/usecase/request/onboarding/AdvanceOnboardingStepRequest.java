package com.dutytracker.usecase.request.onboarding;

import com.dutytracker.domain.model.OnboardingStep;

public record AdvanceOnboardingStepRequest(OnboardingStep currentStep) {}
