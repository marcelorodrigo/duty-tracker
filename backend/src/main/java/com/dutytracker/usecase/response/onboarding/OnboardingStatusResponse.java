package com.dutytracker.usecase.response.onboarding;

import com.dutytracker.domain.model.OnboardingStep;

public record OnboardingStatusResponse(OnboardingStep step, boolean completed) {}
