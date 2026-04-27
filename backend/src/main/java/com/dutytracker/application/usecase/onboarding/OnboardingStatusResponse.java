package com.dutytracker.application.usecase.onboarding;

import com.dutytracker.domain.model.OnboardingStep;

public record OnboardingStatusResponse(OnboardingStep step, boolean completed) {}
